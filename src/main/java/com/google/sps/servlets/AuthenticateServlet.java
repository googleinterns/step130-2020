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
import java.util.HashMap;
import java.util.Map;
import java.lang.Object;

@WebServlet("/authenticate")
public class AuthenticateServlet extends HttpServlet {

  private void MaybeUpdateUserByEmailInDatastore(String userEmail) {
    Map<String, Object> propertyNamesAndValuesToUpdate = new HashMap<String, Object>();

    boolean doesUserEmailExistInDatastore = GivrUser.checkIfUserWithPropertyExists("userEmail", user.getUserEmail());
      if (doesUserEmailExistInDatastore) {
        // User is either a Moderator or Maintainer, that has not logged in.
        boolean isMaintainer = user.isMaintainer();
        if (!isMaintainer) { // User is a Moderator.

          // TODO: Update organization.
        }

        // Update entity with current user's userId.
        propertyNamesAndValuesToUpdate.put("userId", user.getUserId());
        GivrUser.updateUserInDatastore("userEmail", user.getUserEmail(), propertyNamesAndValuesToUpdate);
      }
  }

  private void MaybeUpdateEmailAddressOfUserIdInDatastore(GivrUser currUser) {
    Entity entity = GivrUser.getUserFromDatastoreWithProperty("userId", currUser.getUserId());
    Map<String, Object> propertyNamesAndValuesToUpdate = new HashMap<String, Object>();

    String emailInDatastore = (String) entity.getProperty("userEmail");

    // Ensures that the userEmail is up-to-date.
    if (!currUser.getUserEmail().equals(emailInDatastore)) {
      propertyNamesAndValuesToUpdate.put("userEmail", currUser.getUserEmail());
      GivrUser.updateUserInDatastore("userId", currUser.getUserId(), propertyNamesAndValuesToUpdate);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse  response) throws IOException {
    response.setContentType("application/json;");
    Gson gson = new Gson();

    GivrUser user = GivrUser.getCurrentLoggedInUser();

    boolean doesUserIdExistInDatastore = GivrUser.checkIfUserWithPropertyExists("userId", user.getUserId());
    if (!doesUserIdExistInDatastore) {
      MaybeUpdateUserByEmailInDatastore(user.getUserEmail());
    } else {
      MaybeUpdateEmailAddressOfUserIdInDatastore(user);
    }
    String json = gson.toJson(user);
    response.getWriter().println(json);
  }
}
