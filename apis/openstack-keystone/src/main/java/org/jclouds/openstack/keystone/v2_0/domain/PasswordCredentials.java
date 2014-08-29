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
package org.jclouds.openstack.keystone.v2_0.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.beans.ConstructorProperties;

import org.jclouds.openstack.keystone.v2_0.config.CredentialType;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Objects;

/**
 * Password Credentials
 *
 */
@CredentialType(CredentialTypes.PASSWORD_CREDENTIALS)
public class PasswordCredentials {

   private final String username;
   private final String password;

   @ConstructorProperties({"username", "password"})
   protected PasswordCredentials(String username, String password) {
      this.username = checkNotNull(username, "username");
      this.password = checkNotNull(password, "password");
   }

   /**
    * @return the username
    */
   public String getUsername() {
      return this.username;
   }

   /**
    * @return the password
    */
   public String getPassword() {
      return this.password;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(username, password);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      PasswordCredentials that = PasswordCredentials.class.cast(obj);
      return Objects.equal(this.username, that.username)
            && Objects.equal(this.password, that.password);
   }

   protected ToStringHelper string() {
      return MoreObjects.toStringHelper(this)
            .add("username", username).add("password", password);
   }

   @Override
   public String toString() {
      return string().toString();
   }

   public static Builder builder() {
      return new Builder();
   }

   public Builder toBuilder() {
      return new Builder().fromPasswordCredentials(this);
   }

   public static class Builder {
      protected String username;
      protected String password;

      /**
       * @see PasswordCredentials#getUsername()
       */
      public Builder username(String username) {
         this.username = username;
         return this;
      }

      /**
       * @see PasswordCredentials#getPassword()
       */
      public Builder password(String password) {
         this.password = password;
         return this;
      }

      public PasswordCredentials build() {
         return new PasswordCredentials(username, password);
      }

      public Builder fromPasswordCredentials(PasswordCredentials in) {
         return this.username(in.getUsername()).password(in.getPassword());
      }
   }

   public static PasswordCredentials createWithUsernameAndPassword(String username, String password) {
      return new PasswordCredentials(username, password);
   }

}
