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

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    GivrUser currUser = GivrUser.getCurrentLoggedInUser();

    // User can add another Maintainer only if they are a Maintainer.
    if (currUser.isMaintainer()) {
      String newMaintainerEmail = request.getParameter("userEmail");

      boolean doesNewMaintainerExistInDatastore = GivrUser.checkIfUserWithPropertyExists("userEmail", currUser.getUserEmail());
      if (doesNewMaintainerExistInDatastore) {
        changeMaintainerStatus(newMaintainerEmail);
      } else {
        addNewMaintainerToDatastore(newMaintainerEmail);
      }
      response.setStatus(HttpServletResponse.SC_OK);
      return;
    }
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
    GivrUser.updateUserInDatastore("userEmail", email, "isMaintainer", Boolean.toString(isMaintainer));
  }
}
