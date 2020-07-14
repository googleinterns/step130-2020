// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Transaction;
import java.util.Map;
import java.util.HashMap;
import java.lang.Object;

public final class GivrUser {

  private String id;
  private boolean isMaintainer;
  private boolean isLoggedIn;
  private String url;
  private String email;

  public GivrUser(String id, boolean isMaintainer, boolean isLoggedIn, String url, String email) {
    this.id = id;
    this.isMaintainer = isMaintainer;
    this.isLoggedIn = isLoggedIn;
    this.url = url;
    this.email = email;
  }

  public String getUserId() {
    return this.id;
  }

  public boolean isMaintainer() {
    return this.isMaintainer;
  }

  public String getUserEmail() {
    return this.email;
  }

  public boolean isLoggedIn() {
    return this.isLoggedIn;
  }

  // TODO(): get correct moderator status for organization
  public boolean isModerator(long organizationId) {
      return false;
  }

  public static boolean checkIfUserWithPropertyExists(String propertyName, String propertyValue) {
    if (getUserFromDatastoreWithProperty(propertyName, propertyValue) == null) {
      return false;
    }
    return true;
  }

  // Gets User with propertyName, propertyValue exists within Datastore.
  public static Entity getUserFromDatastoreWithProperty(String propertyName, String propertyValue) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Filter queryFilter = new FilterPredicate(propertyName, FilterOperator.EQUAL, propertyValue);
    Query query = new Query("User").setFilter(queryFilter);
    PreparedQuery preparedQuery = datastore.prepare(query);
    Entity entity = preparedQuery.asSingleEntity();

    return entity; // Entity can be null.
  }

  // Updates a User entity in Datastore, identifying with the first two parameters with values from second two parameters.
  public static void updateUserInDatastore(String identifyingProperty, String identifyingValue, Map<String, Object> updatePropertyNamesAndValues) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = getUserFromDatastoreWithProperty(identifyingProperty, identifyingValue);

    if (entity == null) {
      throw new NoResultException("User with " + identifyingProperty + ": " + identifyingValue + " was not found.");
    }

    Transaction txn = datastore.beginTransaction();
    try {
      for (Map.Entry<String, Object> entry: updatePropertyNamesAndValues.entrySet()) {
        entity.setProperty(entry.getKey(), entry.getValue());
      }

      datastore.put(txn, entity);
      txn.commit();
    } finally {
      if (txn.isActive()) {
        txn.rollback();
        throw new Error("Datastore Update has failed.");
      }
    }
  }

  public static GivrUser getUserById(String userId) {
    Entity entity = getUserFromDatastoreWithProperty("userId", userId);

    boolean isMaintainer = false;
    boolean isLoggedIn = true;
    String userEmail = "";

    if (entity != null) {
      isMaintainer = (boolean) entity.getProperty("isMaintainer");
      userEmail = (String) entity.getProperty("userEmail");
    }

    GivrUser user = new GivrUser(userId, isMaintainer, isLoggedIn, "" /* URL is not needed when User is logged in. */, userEmail);
    return user;
  }

  public static GivrUser getUserByEmail(String email) {
    // TODO: Support OAuth.
    Entity entity = getUserFromDatastoreWithProperty("userEmail", email);

    String userId = "";
    boolean isMaintainer = false;
    boolean isLoggedIn = false;
    String loginURL = "";

    if (entity != null) {
      userId = (String) entity.getProperty("userId");
      isMaintainer = (boolean) entity.getProperty("isMaintainer");
    }
    return new GivrUser(userId, isMaintainer, isLoggedIn, loginURL, email);
  }

  // The email returned in the GivrUser object is the value in the Datastore.
  public static GivrUser getCurrentLoggedInUser() {
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    String url = userService.createLoginURL("/");
    
    if (isUserLoggedIn) {
      return getUserById(userService.getCurrentUser().getUserId());
    }
    return new GivrUser("" /* userId */, false /* isMaintainer */, false /* isLoggedIn */, url /* loginURL */, "" /* userEmail */);
  }
}
