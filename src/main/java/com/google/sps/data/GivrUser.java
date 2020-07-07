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
  private boolean isMaintainer = false;

  public GivrUser(String id, boolean isMaintainer) {
    this.id = id;
    this.isMaintainer = isMaintainer;
  }

  public boolean isMaintainer() {
    return this.isMaintainer;
  }

  public static GivrUser getUserByIdFromDatastore(String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter queryFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query = new Query("User").setFilter(queryFilter);
      
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    PreparedQuery preparedQuery = datastore.prepare(query);
    QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);

    boolean isMaintainer = false;

    if (userResult.size() < 1) {
      return null;
    }
    
    for (Entity entity: preparedQuery.asIterable(fetchOptions)) {
      isMaintainer = (boolean) entity.getProperty("isMaintainer");
    }
    GivrUser user = new GivrUser(userId, isMaintainer);
    return user;
  }

  public static GivrUser getUserByEmail(String email) {
    String authDomain = "gmail.com";
    User user = new User(email, authDomain);
    String userId = user.getUserId();

    return getUserByIdFromDatastore(userId);
  }

  public static GivrUser getLoggedInUser() {
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    
    if (isUserLoggedIn) {
      return getUserByIdFromDatastore(userService.getCurrentUser().getUserId());
    }
    return null;
  }
}
