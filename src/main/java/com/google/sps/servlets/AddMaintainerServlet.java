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
import java.util.HashMap;
import java.util.Map;
import com.google.sps.data.GivrUser;
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

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    GivrUser currUser = GivrUser.getCurrentLoggedInUser();

    if (!currUser.isMaintainer()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // User can add another Maintainer only if they are a Maintainer.
    String newMaintainerEmail = request.getParameter("userEmail");

    Entity entity = GivrUser.getUserFromDatastoreWithProperty("userEmail", newMaintainerEmail);
    if (entity != null) {
      changeMaintainerStatus(newMaintainerEmail);
    } else {
      addNewMaintainerToDatastore(newMaintainerEmail);
    }
    response.sendRedirect("/index.html");
  }

  public void addNewMaintainerToDatastore(String email) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity newUserEntity = new Entity("User");

    newUserEntity.setProperty("userId", "");
    newUserEntity.setProperty("isMaintainer", true);
    newUserEntity.setProperty("userEmail", email);

    datastore.put(newUserEntity);
  }

  public void changeMaintainerStatus(String email) {
    boolean isMaintainer = true;
    Map<String, Object> propertyNamesAndValuesToUpdate = new HashMap<String, Object>();
    propertyNamesAndValuesToUpdate.put("isMaintainer", isMaintainer);
    GivrUser.updateUserInDatastore("userEmail", email, propertyNamesAndValuesToUpdate);
  }
}
