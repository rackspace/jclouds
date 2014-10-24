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
package org.jclouds.openstack.cinder.v1.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.ConstructorProperties;
import java.util.Map;

import javax.inject.Named;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableMap;

/**
 * An Openstack Cinder Volume Type.
 */
public class VolumeType {

   public static Builder builder() {
      return new Builder();
   }

   public Builder toBuilder() {
      return new Builder().fromVolumeType(this);
   }

   public static class Builder {

      protected String id;
      protected String name;
      protected Map<String, String> extraSpecs = ImmutableMap.of();

      /**
       * @see VolumeType#getId()
       */
      public Builder id(String id) {
         this.id = id;
         return this;
      }

      /**
       * @see VolumeType#getName()
       */
      public Builder name(String name) {
         this.name = name;
         return this;
      }

      /**
       * @see VolumeType#getExtraSpecs()
       */
      public Builder extraSpecs(Map<String, String> extraSpecs) {
         this.extraSpecs = ImmutableMap.copyOf(checkNotNull(extraSpecs, "extraSpecs"));
         return this;
      }

      public VolumeType build() {
         return new VolumeType(id, name, extraSpecs);
      }

      public Builder fromVolumeType(VolumeType in) {
         return this
                  .id(in.getId())
                  .name(in.getName())
                  .extraSpecs(in.getExtraSpecs());
      }
   }

   private final String id;
   private final String name;
   @Named("extra_specs")
   private final Map<String, String> extraSpecs;

   @ConstructorProperties({"id", "name", "extra_specs"})
   protected VolumeType(String id, String name, Map<String, String> extraSpecs) {
      this.id = checkNotNull(id, "id");
      this.name = checkNotNull(name, "name");
      this.extraSpecs = extraSpecs == null ? ImmutableMap.<String, String>of() : ImmutableMap.copyOf(extraSpecs);
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public Map<String, String> getExtraSpecs() {
      return this.extraSpecs;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(id, name, extraSpecs);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      VolumeType that = VolumeType.class.cast(obj);
      return Objects.equal(this.id, that.id)
               && Objects.equal(this.name, that.name)
               && Objects.equal(this.extraSpecs, that.extraSpecs);
   }

   protected ToStringHelper string() {
      return Objects.toStringHelper(this)
            .add("id", id).add("name", name).add("extraSpecs", extraSpecs);
   }

   @Override
   public String toString() {
      return string().toString();
   }

}
