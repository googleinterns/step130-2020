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

  public static PreparedQuery getPreparedQueryResultOfUserWithProperty(String propertyName, String propertyValue) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Filter queryFilter = null;
    if (propertyName.equals("isMaintainer")) {
      queryFilter = new FilterPredicate(propertyName, FilterOperator.EQUAL, Boolean.parseBoolean(propertyValue));
    } else {
      queryFilter = new FilterPredicate(propertyName, FilterOperator.EQUAL, propertyValue);
    }
    Query query = new Query("User").setFilter(queryFilter);

    PreparedQuery preparedQuery = datastore.prepare(query);
    return preparedQuery;
  }

  // Check if User with propertyName, propertyValue exists within Datastore.
  public static boolean checkIfUserWithPropertyExists(String propertyName, String propertyValue) {
    PreparedQuery preparedQuery = getPreparedQueryResultOfUserWithProperty(propertyName, propertyValue);
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);

    if (userResult.size() < 1) {
      return false;
    } else if (userResult.size() > 1) {
      throw new IllegalArgumentException("More than one user with the " + propertyName + " was found.");
    }
    return true;
  }

  // Updates a User entity in Datastore, identifying with the first two parameters with values from second two parameters.
  public static void updateUserInDatastore(String identifyingProperty, String identifyingValue, String updateProperty, String updateValue) {  
    if (!checkIfUserWithPropertyExists(identifyingProperty, identifyingValue)) {
      throw new IllegalArgumentException("User with " + identifyingProperty + " of value: " + identifyingValue + " was not found in the Datastore.");
    }

    PreparedQuery preparedQuery = getPreparedQueryResultOfUserWithProperty(identifyingProperty, identifyingValue);
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    for (Entity entity: preparedQuery.asIterable(fetchOptions)) {
      Transaction txn = datastore.beginTransaction();
      try {
        entity.setProperty(updateProperty, updateValue);
        datastore.put(txn, entity);

        txn.commit();
      } finally {
        if (txn.isActive()) {
          txn.rollback();
        }
      }
    }
  }

  public static GivrUser getUserById(String userId) {
    PreparedQuery preparedQuery = getPreparedQueryResultOfUserWithProperty("userId", userId);
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);

    boolean isMaintainer = false;
    boolean isLoggedIn = true;
    String userEmail = "";

    if (userResult.size() == 1) {
      for (Entity entity: preparedQuery.asIterable(fetchOptions)) {
        isMaintainer = (boolean) entity.getProperty("isMaintainer");
        userEmail = (String) entity.getProperty("userEmail");
      }
    } else if (userResult.size() > 1) {
      throw new IllegalArgumentException("More than one user with the userId was found.");
    }

    GivrUser user = new GivrUser(userId, isMaintainer, isLoggedIn, "" /* URL is not needed when User is logged in. */, userEmail);
    return user;
  }

  public static GivrUser getUserByEmail(String email) {
    // TODO: Support OAuth.

    if (!checkIfUserWithPropertyExists("userEmail", email)) {
      return new GivrUser("", false, false, "", email);
    }
    
    PreparedQuery preparedQuery = getPreparedQueryResultOfUserWithProperty("userEmail", email);
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);

    String userId = "";
    boolean isMaintainer = false;
    for (Entity entity: preparedQuery.asIterable(fetchOptions)) {
      userId = (String) entity.getProperty("userId");
      isMaintainer = (boolean) entity.getProperty("isMaintainer");
    }
    return new GivrUser(userId, isMaintainer, false /* isLoggedIn */, "" /* loginURL */, email);
  }

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
