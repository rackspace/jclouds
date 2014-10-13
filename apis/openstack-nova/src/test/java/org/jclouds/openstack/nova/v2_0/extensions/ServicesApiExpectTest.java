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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.jclouds.date.DateService;
import org.jclouds.date.internal.SimpleDateFormatDateService;
import org.jclouds.http.HttpResponse;
import org.jclouds.openstack.nova.v2_0.domain.Service;
import org.jclouds.openstack.nova.v2_0.internal.BaseNovaApiExpectTest;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Collection;

import static org.testng.Assert.assertEquals;

/**
 * Tests ServicesApi guice wiring and parsing
 */
@Test(groups = "unit", testName = "ServicesApiExpectTest")
public class ServicesApiExpectTest extends BaseNovaApiExpectTest {
   private DateService dateService = new SimpleDateFormatDateService();

   public void testList() {
      URI endpoint = URI.create("https://az-1.region-a.geo-1.compute.hpcloudsvc.com/v2/3456/os-services");
      ServicesApi api = requestsSendResponses(keystoneAuthWithUsernameAndPasswordAndTenantName,
            responseWithKeystoneAccess, extensionsOfNovaRequest, extensionsOfNovaResponse,
            authenticatedGET().endpoint(endpoint).build(),
            HttpResponse.builder().statusCode(200).payload(payloadFromResource("/os_services_list.json")).build())
            .getServicesApi("az-1.region-a.geo-1").get();

      Service result = getOsService("host1", api.list().toList());
      assertEquals(result, exampleOsServices());
   }

   public Service exampleOsServices() {
      return Service.builder()
            .binary("nova-scheduler")
            .host("host1").state(Service.State.UP)
            .status(Service.Status.DISABLED)
            .disabledReason("faulty host")
            .updated(dateService.iso8601SecondsDateParse("2012-10-29T13:42:02"))
            .zone("internal")
            .id("1")
            .build();
   }

   /**
    * return os-service by host
    */
   private Service getOsService(final String host, Collection<? extends Service> osServices) {
      Service service = Iterables.getFirst(Iterables.filter(osServices, new Predicate<Object>() {
         @Override
         public boolean apply(Object input) {
            Service service = (Service)input;
            return host.equals(service.getHost());
         }
      }), null);
      return service;
   }
}
