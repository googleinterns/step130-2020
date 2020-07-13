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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import com.google.sps.data.OrganizationUpdater;
import com.google.sps.data.GivrUser;
import com.google.sps.data.HistoryManager;
import java.io.IOException;
import com.google.gson.Gson;
import java.time.Instant;

@WebServlet("/edit-organization")
public class EditOrganizationServlet extends HttpServlet {
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    //Get the proper organization entity for updating
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    long organizationId = Long.parseLong(request.getParameter("id"));
    Key organizationKey = KeyFactory.createKey("Distributor", organizationId);
    Entity organizationEntity = null;

    // try catch for compilation purposes, servlet will not be called without a valid id param
    try {
      organizationEntity = datastore.get(organizationKey);
    } catch(com.google.appengine.api.datastore.EntityNotFoundException err) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }
    
    GivrUser user = GivrUser.getLoggedInUser();

    OrganizationUpdater organizationUpdater = new OrganizationUpdater(organizationEntity);
    long millisecondSinceEpoch = Instant.now().toEpochMilli();
    HistoryManager history = new HistoryManager();
    EmbeddedEntity historyUpdate = history.recordHistory("Organization was edited", millisecondSinceEpoch);
    
    try {
      organizationUpdater.updateOrganization(request, user, /*forRegistration*/ false, historyUpdate);
    } catch(IllegalArgumentException err) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }

    // updates entity with changed properties from the form
    organizationEntity = organizationUpdater.getEntity();

    datastore.put(organizationEntity);
    System.out.println("Edited Organization");
    response.sendRedirect("/organizations.html");
  }
}
