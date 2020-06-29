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
import com.google.sps.data.User;
import com.google.sps.data.LoginInfo;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.FetchOptions;

@WebServlet("/add-user")
public class AddUserServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse  response) throws IOException {
    response.setContentType("application/json;");
    Gson gson = new Gson();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();

    if (isUserLoggedIn) {
      String userEmail = userService.getCurrentUser().getEmail();
      String userId = userService.getCurrentUser().getUserId();
      User currentUser = null;

      boolean doesUserExist = false;
      boolean isUserMaintainer = false;

      Filter queryFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
      Query query = new Query("User").setFilter(queryFilter);
      
      FetchOptions fetchOptions = FetchOptions.Builder.withLimit(1);
      PreparedQuery preparedQuery = datastore.prepare(query);
      QueryResultList<Entity> userResult = preparedQuery.asQueryResultList(fetchOptions);
      if (userResult.size() < 1) {
        User newUser = new User(userId, isUserMaintainer);
        currentUser = newUser;
      
        Entity userEntity = new Entity("User");
        userEntity.setProperty("userId", userId);
        userEntity.setProperty("isMaintainer", isUserMaintainer);
        datastore.put(userEntity);
      }
     
      String urlToRedirectAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectAfterUserLogsOut);
      LoginInfo loginInfo = new LoginInfo(currentUser, true, logoutUrl);
      String json = gson.toJson(loginInfo);
      response.getWriter().println(json);
    } else {
      String urlToRedirectAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectAfterUserLogsIn);
      LoginInfo loginInfo = new LoginInfo(null, false, loginUrl);
      String json = gson.toJson(loginInfo);
      response.getWriter().println(json);
    }
  }
}
