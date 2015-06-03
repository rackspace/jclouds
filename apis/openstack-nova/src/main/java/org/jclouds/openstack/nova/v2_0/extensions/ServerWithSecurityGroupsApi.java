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

import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.jclouds.Fallbacks.EmptyPagedIterableOnNotFoundOr404;
import org.jclouds.Fallbacks.NullOnNotFoundOr404;
import org.jclouds.collect.PagedIterable;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.keystone.v2_0.KeystoneFallbacks.EmptyPaginatedCollectionOnNotFoundOr404;
import org.jclouds.openstack.keystone.v2_0.filters.AuthenticateRequest;
import org.jclouds.openstack.nova.v2_0.domain.ServerWithSecurityGroups;
import org.jclouds.openstack.nova.v2_0.functions.internal.ParseServerDetails;
import org.jclouds.openstack.nova.v2_0.functions.internal.ParseServersWithSecurityGroupsDetails;
import org.jclouds.openstack.v2_0.ServiceType;
import org.jclouds.openstack.v2_0.domain.PaginatedCollection;
import org.jclouds.openstack.v2_0.options.PaginationOptions;
import org.jclouds.openstack.v2_0.services.Extension;
import org.jclouds.rest.annotations.Fallback;
import org.jclouds.rest.annotations.RequestFilters;
import org.jclouds.rest.annotations.ResponseParser;
import org.jclouds.rest.annotations.SelectJson;
import org.jclouds.rest.annotations.Transform;

import com.google.common.annotations.Beta;

/**
 * Provides access to the OpenStack Compute (Nova) Create Server extension API.
 *
 * This provides details including the security groups associated with a Server.
 * <p/>
 *
 * NOTE: the equivalent to listServersInDetail() isn't available at the other end, so not
 * extending ServerApi at this time.
 *
 * @see org.jclouds.openstack.nova.v2_0.features.ServerApi
 */
@Beta
@Extension(of = ServiceType.COMPUTE, namespace = ExtensionNamespaces.CREATESERVEREXT)
@RequestFilters(AuthenticateRequest.class)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/os-create-server-ext")
public interface ServerWithSecurityGroupsApi {
   /**
    * Retrieve details of the specified server, including security groups
    *
    * @param id id of the server
    * @return server or null if not found
    */
   @Named("server:get")
   @GET
   @SelectJson("server")
   @Path("/{id}")
   @Fallback(NullOnNotFoundOr404.class)
   @Nullable
   ServerWithSecurityGroups get(@PathParam("id") String id);

   /**
    * List all servers (all details)
    *
    * @return all servers (all details)
    */
   @Named("server:list")
   @GET
   @Path("/detail")
   @ResponseParser(ParseServersWithSecurityGroupsDetails.class)
   @Transform(ParseServersWithSecurityGroupsDetails.ToPagedIterable.class)
   @Fallback(EmptyPagedIterableOnNotFoundOr404.class)
   PagedIterable<ServerWithSecurityGroups> listInDetail();

   /**
    * List all servers (all details)
    *
    * @param options Options that describe how the PaginatedCollection is retrieved from the service, such as marker or
    *                limit.
    * @return all servers (all details)
    */
   @Named("server:list")
   @GET
   @Path("/detail")
   @ResponseParser(ParseServerDetails.class)
   @Fallback(EmptyPaginatedCollectionOnNotFoundOr404.class)
   PaginatedCollection<ServerWithSecurityGroups> listInDetail(PaginationOptions options);
}
