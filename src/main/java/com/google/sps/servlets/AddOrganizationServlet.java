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
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.util.ArrayList;
import com.google.sps.data.OrganizationUpdater;
import com.google.sps.data.GivrUser;
import java.io.IOException;

@WebServlet("/add-organization")
public class AddOrganizationServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    GivrUser user = GivrUser.getLoggedInUser();

    if (user.getUserId().equals("")) {
      throw new IllegalArgumentException("Error: unable to register organization if user is not logged in.");
    }

    // when suppliers are added, Entity kind will be from a parameter- for now is hardcoded
    Entity newOrganizationEntity = new Entity("Distributor");

    OrganizationUpdater organizationUpdater = new OrganizationUpdater(newOrganizationEntity);
    
    // update rest of organization properties from inputted form
    try {
      organizationUpdater.updateOrganization(request, user, /*forRegistration*/ true);
    } catch(IllegalArgumentException err) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }

    newOrganizationEntity = organizationUpdater.getEntity();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newOrganizationEntity);

    response.sendRedirect("/index.html");
  }
}
