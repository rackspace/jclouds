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
package org.jclouds.openstack.nova.v2_0.domain;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.jclouds.javax.annotation.Nullable;

import javax.inject.Named;
import java.beans.ConstructorProperties;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Os-Service can be manipulated using the os-services Extension to Nova (alias "SERVICES")
 *
 * @see org.jclouds.openstack.nova.v2_0.extensions.ServicesApi
 */
public class Service {

   public static enum Status {
      ENABLED, DISABLED, UNRECOGNIZED;

      public String value() {
         return name().toLowerCase();
      }

      @Override
      public String toString() {
         return value();
      }

      public static Status fromValue(String state) {
         try {
            return valueOf(checkNotNull(state, "status").toUpperCase());
         } catch (IllegalArgumentException e) {
            return UNRECOGNIZED;
         }
      }
   }

   public static enum State {
      UP, DOWN, UNRECOGNIZED;

      public String value() {
         return name().toLowerCase();
      }

      @Override
      public String toString() {
         return value();
      }

      public static State fromValue(String state) {
         try {
            return valueOf(checkNotNull(state, "state").toUpperCase());
         } catch (IllegalArgumentException e) {
            return UNRECOGNIZED;
         }
      }
   }

   public static Builder builder() {
      return new Builder();
   }

   public Builder toBuilder() {
      return new Builder().fromService(this);
   }

   public static class Builder {

      protected String id;
      protected String binary;
      protected String host;
      protected State state;
      protected Status status;
      protected Date updated;
      protected String zone;
      protected String disabledReason;

      public Builder id(String id) {
         this.id = id;
         return this;
      }

      public Builder binary(String binary) {
         this.binary = binary;
         return this;
      }

      public Builder host(String host) {
         this.host = host;
         return this;
      }

      public Builder state(State state) {
         this.state = state;
         return this;
      }

      public Builder status(Status status) {
         this.status = status;
         return this;
      }

      public Builder updated(Date updated) {
         this.updated = updated;
         return this;
      }

      public Builder zone(String zone) {
         this.zone = zone;
         return this;
      }

      public Builder disabledReason(String disabledReason) {
         this.disabledReason = disabledReason;
         return this;
      }

      public Service build() {
         return new Service(id, binary, host, state, status, disabledReason, updated, zone);
      }

      public Builder fromService(Service in) {
         return this
               .id(in.getId())
               .binary(in.getBinary())
               .host(in.getHost())
               .state(in.getState())
               .status(in.getStatus())
               .disabledReason(in.getDisabledReason())
               .updated(in.getUpdated().isPresent() ? in.getUpdated().get() : null)
               .zone(in.getZone());
      }
   }

   protected String id;
   protected String binary;
   protected String host;
   protected State state;
   protected Status status;
   @Named("disabled_reason")
   protected String disabledReason;
   @Named("updated_at")
   private final Optional<Date> updated;
   protected String zone;

   @ConstructorProperties({ "id", "binary", "host", "state", "status", "disabled_reason", "updated_at", "zone"
   })
   public Service(String id, String binary, String host, State state, Status status, String disabledReason, @Nullable Date updated, String zone) {
      this.id = id;
      this.binary = Preconditions.checkNotNull(binary);
      this.host = Preconditions.checkNotNull(host);
      this.state = state;
      this.status = Preconditions.checkNotNull(status);
      this.disabledReason = disabledReason;
      this.updated = Optional.fromNullable(updated);
      this.zone = zone;
   }

   public String getId() {
      return id;
   }

   public String getBinary() {
      return binary;
   }

   public String getHost() {
      return host;
   }

   public State getState() {
      return state;
   }

   public Status getStatus() {
      return status;
   }

   public String getDisabledReason() {
      return disabledReason;
   }

   public Optional<Date> getUpdated() {
      return updated;
   }

   public String getZone() {
      return zone;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Service service = (Service) o;

      return Objects.equal(this.id, service.id) &&
            Objects.equal(this.binary, service.binary) &&
            Objects.equal(this.host, service.host) &&
            Objects.equal(this.state, service.state) &&
            Objects.equal(this.status, service.status) &&
            Objects.equal(this.disabledReason, service.disabledReason) &&
            Objects.equal(this.updated, service.updated) &&
            Objects.equal(this.zone, service.zone);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(binary, host, state, status, disabledReason, updated, zone, id);
   }

   protected Objects.ToStringHelper string() {
      return Objects.toStringHelper(this)
            .add("id", id).add("binary", binary).add("state", state).add("status", status).add("disabledReason", disabledReason)
            .add("updated", updated).add("zone", zone);
   }

   @Override
   public String toString() {
      return string().toString();
   }

}
