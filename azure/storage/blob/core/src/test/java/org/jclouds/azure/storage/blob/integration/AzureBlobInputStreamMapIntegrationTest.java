/**
 *
 * Copyright (C) 2009 Global Cloud Specialists, Inc. <info@globalcloudspecialists.com>
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
 * ====================================================================
 */
package org.jclouds.azure.storage.blob.integration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.jclouds.azure.storage.blob.AzureBlobStore;
import org.jclouds.azure.storage.blob.domain.Blob;
import org.jclouds.azure.storage.blob.domain.BlobMetadata;
import org.jclouds.azure.storage.blob.domain.ContainerMetadata;
import org.jclouds.blobstore.integration.internal.BaseInputStreamMapIntegrationTest;
import org.testng.annotations.Test;

/**
 * @author Adrian Cole
 */
@Test(groups = { "integration", "live" }, testName = "cloudfiles.AzureBlobInputStreamMapIntegrationTest")
public class AzureBlobInputStreamMapIntegrationTest extends
         BaseInputStreamMapIntegrationTest<AzureBlobStore, ContainerMetadata, BlobMetadata, Blob> {

   @Override
   public void testContainsBytesValue() throws InterruptedException, ExecutionException,
            TimeoutException {
      // http://code.google.com/p/jclouds/issues/detail?id=90
   }

   @Override
   public void testContainsFileValue() throws InterruptedException, ExecutionException,
            TimeoutException {
      // http://code.google.com/p/jclouds/issues/detail?id=90
   }

   @Override
   public void testContainsInputStreamValue() throws InterruptedException, ExecutionException,
            TimeoutException {
      // http://code.google.com/p/jclouds/issues/detail?id=90
   }

   @Override
   public void testContainsStringValue() throws InterruptedException, ExecutionException,
            TimeoutException {
      // http://code.google.com/p/jclouds/issues/detail?id=90
   }
}