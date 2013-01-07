/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jclouds.azureblob.blobstore;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.jclouds.blobstore.util.BlobStoreUtils.cleanRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.azureblob.AzureBlobAsyncClient;
import org.jclouds.azureblob.blobstore.functions.BlobToAzureBlob;
import org.jclouds.azureblob.domain.AzureBlob;
import org.jclouds.blobstore.BlobRequestSigner;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.functions.BlobToHttpGetOptions;
import org.jclouds.http.HttpRequest;
import org.jclouds.http.options.GetOptions;
import org.jclouds.reflect.Invocation;
import org.jclouds.rest.internal.RestAnnotationProcessor;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;

/**
 * 
 * @author Adrian Cole
 */
@Singleton
public class AzureBlobRequestSigner implements BlobRequestSigner {
   private final RestAnnotationProcessor processor;
   private final BlobToAzureBlob blobToBlob;
   private final BlobToHttpGetOptions blob2HttpGetOptions;

   private final Invokable<?, ?> getMethod;
   private final Invokable<?, ?> deleteMethod;
   private final Invokable<?, ?> createMethod;

   @Inject
   public AzureBlobRequestSigner(RestAnnotationProcessor processor, BlobToAzureBlob blobToBlob,
         BlobToHttpGetOptions blob2HttpGetOptions) throws SecurityException, NoSuchMethodException {
      this.processor = checkNotNull(processor, "processor");
      this.blobToBlob = checkNotNull(blobToBlob, "blobToBlob");
      this.blob2HttpGetOptions = checkNotNull(blob2HttpGetOptions, "blob2HttpGetOptions");
      this.getMethod = Invokable.from(AzureBlobAsyncClient.class.getMethod("getBlob", String.class, String.class,
            GetOptions[].class));
      this.deleteMethod = Invokable
            .from(AzureBlobAsyncClient.class.getMethod("deleteBlob", String.class, String.class));
      this.createMethod = Invokable
            .from(AzureBlobAsyncClient.class.getMethod("putBlob", String.class, AzureBlob.class));
   }

   @Override
   public HttpRequest signGetBlob(String container, String name) {
      checkNotNull(container, "container");
      checkNotNull(name, "name");
      return cleanRequest(processor.apply(Invocation.create(getMethod, ImmutableList.<Object> of(container, name))));
   }

   @Override
   public HttpRequest signGetBlob(String container, String name, long timeInSeconds) {
      throw new UnsupportedOperationException();
   }

   @Override
   public HttpRequest signPutBlob(String container, Blob blob) {
      checkNotNull(container, "container");
      checkNotNull(blob, "blob");
      return cleanRequest(processor.apply(Invocation.create(createMethod,
            ImmutableList.<Object> of(container, blobToBlob.apply(blob)))));
   }

   @Override
   public HttpRequest signPutBlob(String container, Blob blob, long timeInSeconds) {
      throw new UnsupportedOperationException();
   }

   @Override
   public HttpRequest signRemoveBlob(String container, String name) {
      checkNotNull(container, "container");
      checkNotNull(name, "name");
      return cleanRequest(processor.apply(Invocation.create(deleteMethod, ImmutableList.<Object> of(container, name))));
   }

   @Override
   public HttpRequest signGetBlob(String container, String name, org.jclouds.blobstore.options.GetOptions options) {
      checkNotNull(container, "container");
      checkNotNull(name, "name");
      return cleanRequest(processor.apply(Invocation.create(getMethod,
            ImmutableList.of(container, name, blob2HttpGetOptions.apply(checkNotNull(options, "options"))))));
   }
}
