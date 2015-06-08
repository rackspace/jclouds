/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.openstack.swift.v1.strategy.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Provider;

import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.internal.BlobRuntimeException;
import org.jclouds.blobstore.reference.BlobStoreConstants;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;
import org.jclouds.logging.Logger;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Segment;
import org.jclouds.openstack.swift.v1.features.ObjectApi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ParallelMultipartUploadStrategy implements MultipartUploadStrategy {

   public static final int RETRIES = 10;
   public static final int THREADS = 10;
   public static final int WORK_QUEUE_SIZE = 10000;


   @Resource
   @Named(BlobStoreConstants.BLOBSTORE_LOGGER)
   private Logger logger = Logger.NULL;

   private final SwiftApi client;
   private final Provider<BlobBuilder> blobBuilders;
   private final MultipartUploadSlicingAlgorithm algorithm;
   private final PayloadSlicer slicer;
   private final MultipartNamingStrategy namingStrategy;

   @Inject
   public ParallelMultipartUploadStrategy(SwiftApi client, Provider<BlobBuilder> blobBuilders,
         MultipartUploadSlicingAlgorithm algorithm, PayloadSlicer slicer,
         MultipartNamingStrategy namingStrategy, @Assisted String regionId) {
      this.client = checkNotNull(client, "client");
      this.blobBuilders = checkNotNull(blobBuilders, "blobBuilders");
      this.algorithm = checkNotNull(algorithm, "algorithm");
      this.slicer = checkNotNull(slicer, "slicer");
      this.namingStrategy = checkNotNull(namingStrategy, "namingStrategy");
   }

   /**
    * This uses the same approach as the sequential upload strategy. However, in this case, the upload work is separated
    * into threadable work units (retries included). All the work is queued: up to THREADS units executed in parallel;
    * up to WORK_QUEUE_SIZE units generated in the to-do-work buffer (to avoid memory issues).
    * After all the work units are queued, we start getting the results. We get the results sequentially, as we should
    * not be getting a substantial speed-up from building the manifest in parallel (most of the work is already done).
    *
    * @param regionId
    * @param container
    * @param blob
    * @return
    */
   @Override
   public String execute(String regionId, String container, Blob blob) {
      String key = blob.getMetadata().getName();
      Payload payload = blob.getPayload();
      Long length = payload.getContentMetadata().getContentLength();
      checkNotNull(length,
            "please invoke payload.getContentMetadata().setContentLength(length) prior to multipart upload");
      long chunkSize = algorithm.calculateChunkSize(length);
      int partCount = algorithm.getParts();
      if (partCount > 0) {
         ImmutableList.Builder<Segment> segmentsBuilder = ImmutableList.builder();

         // Submit upload tasks to the executor
         ExecutorService threadPoolExecutor =
               new ThreadPoolExecutor(
                     THREADS,
                     THREADS,
                     1000,
                     TimeUnit.MILLISECONDS,
                     new LinkedBlockingQueue<Runnable>(WORK_QUEUE_SIZE)
               );
         // Store future results here
         LinkedList<Future<Segment>> results = new LinkedList<Future<Segment>>();

         // Queue all the work. This will block if there are more than 10000 parts
         for (Payload part : slicer.slice(payload, chunkSize)) {
            int partNum = algorithm.getNextPart();
            String partName = namingStrategy.getPartName(key, partNum, partCount);
            part.getContentMetadata().setContentDisposition(partName);

            results.add(threadPoolExecutor
                  .submit(new UploadThread(client.getObjectApi(regionId, container), partName, part)));
         }

         // Collect results. This blocks and waits on a result if a result is not ready.
         for (Future<Segment> result : results) {
            Segment segment = null;
            try {
               segment = result.get();
            } catch (InterruptedException e) {
               e.printStackTrace();
               throw new BlobRuntimeException("Upload interrupted", e);
            } catch (ExecutionException e) {
               e.printStackTrace();
               throw new BlobRuntimeException("Upload interrupted", e);
            }
            segmentsBuilder.add(segment);
         }

         return client.getStaticLargeObjectApi(regionId, container).replaceManifest(key, segmentsBuilder.build(),
               ImmutableMap.<String, String>of());
      } else {
         return client.getObjectApi(regionId, container).put(key, payload);
      }
   }

   private static class UploadThread implements Callable<Segment> {
      @Resource
      @Named(BlobStoreConstants.BLOBSTORE_LOGGER)
      private Logger logger = Logger.NULL;

      private ThreadLocal<ObjectApi> objectApi;
      private ThreadLocal<String> partName;
      private ThreadLocal<Payload> part;

      public UploadThread(ObjectApi objectApi, String key, Payload payload) {
         if (objectApi != null) this.objectApi = new ThreadLocal<ObjectApi>();
         if (key != null) this.partName = new ThreadLocal<String>();
         if (payload != null) this.part = new ThreadLocal<Payload>();

         this.objectApi.set(objectApi);
         this.partName.set(key);
         this.part.set(payload);
      }

      /**
       * Computes a result, or throws an exception if unable to do so.
       *
       * @return computed result
       * @throws Exception if unable to compute a result
       */
      @Override
      public Segment call() throws Exception {
         // return the etag
         Exception lastException = null;
         for (int n = 0; n < RETRIES; n++) {
            try {
               return Segment.builder()
                     .etag(objectApi.get().put(partName.get(), part.get()))
                     .path(partName.get())
                     .sizeBytes(part.get().getContentMetadata().getContentLength())
                     .build();

            } catch (Exception e) {
               logger.error(e, "Exception while uploading part " + partName);
               lastException = e;
            }
         }
         throw new BlobRuntimeException("Could not upload a part after " + RETRIES + " retries", lastException);
      }
   }
}
