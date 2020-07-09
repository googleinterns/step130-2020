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

public final class GivrUser {
  private String id;
  private boolean isLoggedIn;
  private String url;

  public GivrUser(String id, boolean isLoggedIn, String url) {
    this.id = id;
    this.isLoggedIn = isLoggedIn;
    this.url = url;
    // TODO: Add email attribute, but do not add to the Datastore.
  }

  public String getUserId() {
    return this.id;
  }

  public static GivrUser getUserById(String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter queryFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query = new Query("MaintainerUser").setFilter(queryFilter);
      
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    PreparedQuery preparedQuery = datastore.prepare(query);
    QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);

    boolean isMaintainer = false;
    boolean isLoggedIn = true;

    if (userResult.size() == 1) {
      for (Entity entity: preparedQuery.asIterable(fetchOptions)) {
        isMaintainer = true;
      }
    } else {
      throw new IllegalArgumentException("More than one user with the userId was found.");
    }

    GivrUser user = new GivrUser(userId, isLoggedIn, "");
    return user;
  }

  public static GivrUser getUserByEmail(String email) {
    // TODO: Support OAuth.
    String authDomain = "gmail.com";
    User user = new User(email, authDomain);
    String userId = user.getUserId();

    return getUserById(userId);
  }

  public static GivrUser getLoggedInUser() {
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    
    if (isUserLoggedIn) {
      return getUserById(userService.getCurrentUser().getUserId());
    }

    String loginUrl = userService.createLoginURL("/");
    return new GivrUser("", isUserLoggedIn, loginUrl);
  }
}
