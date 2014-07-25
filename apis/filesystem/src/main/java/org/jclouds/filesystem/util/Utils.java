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
package org.jclouds.filesystem.util;

import com.google.common.util.concurrent.Uninterruptibles;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for the filesystem blobstore.
 */
public class Utils {
   /** Private constructor for utility class. */
   private Utils() {
      // Do nothing
   }

   /** Delete a file or a directory recursively. */
   public static void deleteRecursively(File file) throws IOException {
      if(!file.exists())
         return;
      if (file.isDirectory()) {
         File[] children = file.listFiles();
         if (children != null) {
            for (File child : children) {
               deleteRecursively(child);
            }
         }
      }

      /**
       * Java-6 Compatible version of handling non-empty directories and access-denied on windows.
       */
      if(!file.delete()){
         if(file.isDirectory() && file.listFiles().length > 0) {
            deleteRecursively(file);
         } else {
            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.SECONDS);
            if(!file.delete())
               file.deleteOnExit();
         }
      }
   }
}
