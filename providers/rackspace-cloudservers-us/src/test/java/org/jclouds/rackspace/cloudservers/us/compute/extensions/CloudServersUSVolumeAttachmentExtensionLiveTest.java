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
package org.jclouds.rackspace.cloudservers.us.compute.extensions;

import static org.jclouds.util.Predicates2.retry;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.cinder.v1.CinderApi;
import org.jclouds.openstack.cinder.v1.domain.Volume;
import org.jclouds.openstack.cinder.v1.features.VolumeApi;
import org.jclouds.openstack.cinder.v1.options.CreateVolumeOptions;
import org.jclouds.openstack.nova.v2_0.domain.VolumeAttachment;
import org.jclouds.openstack.nova.v2_0.extensions.VolumeAttachmentApi;
import org.jclouds.openstack.nova.v2_0.extensions.VolumeAttachmentApiLiveTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Uninterruptibles;

@Test(groups = "live", singleThreaded = true, testName = "CloudServersUSVolumeAttachmentExtensionLivetest")
public class CloudServersUSVolumeAttachmentExtensionLiveTest extends VolumeAttachmentApiLiveTest {

   public CloudServersUSVolumeAttachmentExtensionLiveTest() {
      provider = "rackspace-cloudservers-us";
   }

   private VolumeApi volumeApi;
   private Optional<? extends VolumeAttachmentApi> volumeAttachmentApi;

   private String region;
   private Volume testVolume;
   private CinderApi cinderApi;

   @BeforeClass(groups = {"integration", "live"})
   @Override
   public void setup() {
      super.setup();
      region = Iterables.getLast(api.getConfiguredRegions(), "nova");

      cinderApi = ContextBuilder.newBuilder("rackspace-cloudblockstorage-us")
            .credentials(identity, credential)
            .buildApi(CinderApi.class);
      volumeApi = cinderApi.getVolumeApi(region);
      volumeAttachmentApi = api.getVolumeAttachmentApi(region);
   }

   @AfterClass(groups = { "integration", "live" })
   @Override
   protected void tearDown() {
         if (testVolume != null) {
            final String volumeId = testVolume.getId();
            assertTrue(volumeApi.delete(volumeId));
            assertTrue(retry(new Predicate<VolumeApi>() {
               public boolean apply(VolumeApi volumeApi) {
                  return volumeApi.get(volumeId) == null;
               }
            }, 180 * 1000L).apply(volumeApi));
         }
      super.tearDown();
   }

   public void testCreateVolume() {
         CreateVolumeOptions options = CreateVolumeOptions.Builder
               .name("jclouds-test-volume")
               .description("description of test volume");

         testVolume = volumeApi.create(100, options);
         assertTrue(retry(new Predicate<VolumeApi>() {
            public boolean apply(VolumeApi volumeApi) {
               return volumeApi.get(testVolume.getId()).getStatus() == Volume.Status.AVAILABLE;
            }
         }, 180 * 1000L).apply(volumeApi));
   }

   @Test(dependsOnMethods = "testCreateVolume")
   public void testAttachments() {
         String server_id = null;
         try {
            final String serverId = server_id = createServerInRegion(region).getId();

            Set<? extends VolumeAttachment> attachments =
                  volumeAttachmentApi.get().listAttachmentsOnServer(serverId).toSet();
            assertNotNull(attachments);
            final int before = attachments.size();

            VolumeAttachment testAttachment = volumeAttachmentApi.get().attachVolumeToServerAsDevice(
                  testVolume.getId(), serverId, "/dev/vdf");
            assertNotNull(testAttachment.getId());
            assertEquals(testAttachment.getVolumeId(), testVolume.getId());

            Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
            assertTrue(retry(new Predicate<VolumeApi>() {
               public boolean apply(VolumeApi volumeApi) {
                  return volumeApi.get(testVolume.getId()).getStatus().equals(Volume.Status.IN_USE);
               }
            }, 60 * 1000L).apply(volumeApi));

            attachments = volumeAttachmentApi.get().listAttachmentsOnServer(serverId).toSet();
            assertNotNull(attachments);
            assertEquals(attachments.size(), before + 1);

            assertEquals(volumeApi.get(testVolume.getId()).getStatus(), Volume.Status.IN_USE);

            boolean foundIt = false;
            for (VolumeAttachment att : attachments) {
               VolumeAttachment details = volumeAttachmentApi.get()
                     .getAttachmentForVolumeOnServer(att.getVolumeId(), serverId);
               assertNotNull(details);
               assertNotNull(details.getId());
               assertNotNull(details.getServerId());
               assertNotNull(details.getVolumeId());
               if (Objects.equal(details.getVolumeId(), testVolume.getId())) {
                  foundIt = true;
                  assertNotNull(details.getDevice());
                  assertEquals(details.getServerId(), serverId);
               }
            }

            assertTrue(foundIt, "Failed to find the attachment we created in listAttachments() response");

            volumeAttachmentApi.get().detachVolumeFromServer(testVolume.getId(), serverId);
            assertTrue(retry(new Predicate<VolumeAttachmentApi>() {
               public boolean apply(VolumeAttachmentApi volumeAttachmentApi) {
                  return volumeAttachmentApi.listAttachmentsOnServer(serverId).size() == before;
               }
            }, 60 * 1000L).apply(volumeAttachmentApi.get()));

         } finally {
            if (server_id != null)
               api.getServerApi(region).delete(server_id);
         }
      }
}
