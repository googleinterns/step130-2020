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
import com.google.sps.data.GivrUser;
import com.google.sps.data.UserLoginInfo;
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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@WebServlet("/authenticate")
public class AuthenticateServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse  response) throws IOException {
    response.setContentType("application/json;");
    Gson gson = new Gson();

    UserService userService = UserServiceFactory.getUserService();
    GivrUser user = GivrUser.getLoggedInUser();
    boolean isLoggedIn;
    String url;
    // User is null when they are not logged in.
    if (user == null) {
      String urlToRedirectAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectAfterUserLogsIn);
      isLoggedIn = false;
      url = loginUrl;
     
    } else {
      String urlToRedirectAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectAfterUserLogsOut);
      isLoggedIn = true;
      url = logoutUrl;
    }
    UserLoginInfo loginInfo = new UserLoginInfo(user, isLoggedIn, url);
    String json = gson.toJson(loginInfo);
    response.getWriter().println(json);
  }
}
