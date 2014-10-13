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
package org.jclouds.openstack.nova.v2_0.extensions;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jclouds.openstack.nova.v2_0.domain.Service;
import org.jclouds.openstack.nova.v2_0.internal.BaseNovaApiLiveTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests behavior of ServicesApi
 */
@Test(groups = "live", testName = "ServicesApiLiveTest", singleThreaded = true)
public class ServicesApiLiveTest extends BaseNovaApiLiveTest {

   public static final String INTERNAL = "internal";

   @Test
   public void testList() {
      for (String regionId : api.getConfiguredZones()) {
         Optional<? extends ServicesApi> apiOption = api.getServicesApi(regionId);
         assertTrue(apiOption.isPresent());
         ServicesApi api = apiOption.get();
         Set<? extends Service> osServices = api.list().toSet();

         assertNotNull(osServices);
         for (Service service : osServices) {
            assertNotNull(service.getBinary());
            assertNotNull(service.getHost());
            assertNotNull(service.getState());
            assertNotNull(service.getZone());
            assertNotNull(service.getStatus());
            assertNotNull(service.getUpdated());
            assertNotNull(service.getId());
         }
      }
   }

   @Test
   public void testEnableDisable() {
      for (String regionId : api.getConfiguredZones()) {
         Optional<? extends ServicesApi> apiOption = api.getServicesApi(regionId);
         assertTrue(apiOption.isPresent());
         ServicesApi api = apiOption.get();
         List<? extends Service> osServices = api.list().toList();
         assertNotNull(osServices);
         Service service = Iterables.getFirst(Iterables.filter(osServices, new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
               Service service = (Service) input;
               return Service.Status.ENABLED.equals(service.getStatus()) && !INTERNAL.equals(service.getZone());
            }
         }), null);

         try {
            Service serviceDisabled = api.disable(service.getHost(), service.getBinary());
            assertEquals(Service.Status.DISABLED, serviceDisabled.getStatus());
         }
         finally {
            Service serviceEnabled = api.enable(service.getHost(), service.getBinary());
            assertEquals(Service.Status.ENABLED, serviceEnabled.getStatus());
         }
      }
   }
}
