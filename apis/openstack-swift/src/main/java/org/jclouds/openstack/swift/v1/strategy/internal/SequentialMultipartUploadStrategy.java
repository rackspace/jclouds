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

import javax.annotation.Resource;
import javax.inject.Named;
import javax.inject.Provider;

import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.reference.BlobStoreConstants;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;
import org.jclouds.logging.Logger;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Segment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class SequentialMultipartUploadStrategy implements MultipartUploadStrategy {

   @Resource
   @Named(BlobStoreConstants.BLOBSTORE_LOGGER)
   private Logger logger = Logger.NULL;

   private final SwiftApi client;
   private final Provider<BlobBuilder> blobBuilders;
   private final MultipartUploadSlicingAlgorithm algorithm;
   private final PayloadSlicer slicer;
   private final MultipartNamingStrategy namingStrategy;

   @Inject
   public SequentialMultipartUploadStrategy(SwiftApi client, Provider<BlobBuilder> blobBuilders,
         MultipartUploadSlicingAlgorithm algorithm, PayloadSlicer slicer,
         MultipartNamingStrategy namingStrategy, @Assisted String regionId) {
      this.client = checkNotNull(client, "client");
      this.blobBuilders = checkNotNull(blobBuilders, "blobBuilders");
      this.algorithm = checkNotNull(algorithm, "algorithm");
      this.slicer = checkNotNull(slicer, "slicer");
      this.namingStrategy = checkNotNull(namingStrategy, "namingStrategy");
   }

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
         for (Payload part : slicer.slice(payload, chunkSize)) {
            int partNum = algorithm.getNextPart();
            String partName = namingStrategy.getPartName(key, partNum, partCount);
            part.getContentMetadata().setContentDisposition(partName);
            String eTag = client.getObjectApi(regionId, container).put(partName, part);
            segmentsBuilder.add(
                  Segment.builder().path(partName).etag(eTag).sizeBytes(part.getContentMetadata().getContentLength())
                        .build());
         }

         return client.getStaticLargeObjectApi(regionId, container).replaceManifest(key, segmentsBuilder.build(),
               ImmutableMap.<String, String>of());
      } else {
         return client.getObjectApi(regionId, container).put(key, payload);
      }
   }
}
