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

package com.google.sps.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.util.ArrayList;
import com.google.sps.data.User;
import java.io.IOException;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;

@WebServlet("/add-maintainer")
public class AddMaintainerServlet extends HttpServlet {

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    response.setContentType("application/json;");

    if (isUserLoggedIn) {
      String userId = userService.getCurrentUser().getUserId();
      User currUser = getUser(userId);
      if (currUser.getMaintainerStatus()) {
        System.out.println("IS MAINTAINER");
        String newMaintainerEmail = request.getParameter("user-email");
        changeMaintainerStatus(newMaintainerEmail);
      } else {
        System.out.println("IS NOT MAINTAINER");
        // TODO: send a message disallowing this addition , or put a check before
      }
    }

    // if user is maintainer, go on, if not, then this request cannot be met

    // 
  }

  public void changeMaintainerStatus(String email) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter queryFilter = new FilterPredicate("userEmail", FilterOperator.EQUAL, email);
    Query query = new Query("User").setFilter(queryFilter);
      
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    PreparedQuery preparedQuery = datastore.prepare(query);
    QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);

    boolean isMaintainer = false;
    String userId;
    for (Entity entity: preparedQuery.asIterable(fetchOptions)) {
      userId = (String) entity.getProperty("userId");
      isMaintainer = (boolean) entity.getProperty("isMaintainer");
    }

    Entity updatedUserEntity = new Entity("User");
    updatedUserEntity.setProperty("userId", userId);
    updatedUserEntity.setProperty("isMaintainer", !isMaintainer);
    updatedUserEntity.setProperty("userEmail", email);
    datastore.put(updatedUserEntity);
  }

  public User getUser(String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Filter queryFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query = new Query("User").setFilter(queryFilter);
      
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
    PreparedQuery preparedQuery = datastore.prepare(query);
    QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);

    boolean isMaintainer = false;
    for (Entity entity: preparedQuery.asIterable(fetchOptions)) {
      isMaintainer = (boolean) entity.getProperty("isMaintainer");
    }
    User user = new User(userId, isMaintainer);
    return user;
  }
}
