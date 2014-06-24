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
package org.jclouds.blobstore.config;

public class BlobStoreProperties {
   /**
    * The size (in bytes) of object parts being uploaded in parallel.
    */
   public static final String PROPERTY_MPU_PARTS_SIZE = "jclouds.mpu.parts.size";
   /**
    * The magnitude of object parts being uploaded in parallel.
    */
   public static final String PROPERTY_MPU_PARTS_MAGNITUDE = "jclouds.mpu.parts.magnitude";
   /**
    * The number of object parts being uploaded in parallel.
    */
   public static final String PROPERTY_MPU_PARALLEL_DEGREE = "jclouds.mpu.parallel.degree";
   /**
    * The minimum number of retries when uploading an object part.
    */
   public static final String PROPERTY_MPU_PARALLEL_RETRIES_MIN = "jclouds.mpu.parallel.retries.min";
   /**
    * The maximum percentage of retries when uploading an object part.
    */
   public static final String PROPERTY_MPU_PARALLEL_RETRIES_MAX_PERCENT = "jclouds.mpu.parallel.retries.maxpercent";
}
