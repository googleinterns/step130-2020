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
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import com.google.sps.data.User;
import com.google.sps.data.HistoryManager;
import com.google.sps.data.OrganizationUpdater;
import com.google.sps.data.GivrUser;
import java.time.Instant;
import java.io.IOException;

@WebServlet("/add-organization")
public class AddOrganizationServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    GivrUser user = GivrUser.getLoggedInUser();

    if (user.getUserId().equals("")) {
      throw new IllegalArgumentException("Error: unable to register organization if user is not logged in.");
    }
    

    /* MillisecondSinceEpoch represent the number of milliseconds that have passed since
     * 00:00:00 UTC on January 1, 1970. It ensures that all users are entering a representation
     * of time that is independent of their time zone */
    long millisecondSinceEpoch = Instant.now().toEpochMilli();

    // when suppliers are added, Entity kind will be from a parameter- for now is hardcoded
    Entity newOrganizationEntity = new Entity("Distributor");

    /* This implementation stores history entries as embedded entities instead of custom objects
     * because it is much simpler that way */
    ArrayList changeHistory = new ArrayList<>();
    
    HistoryManager history = new HistoryManager();

    changeHistory.add(history.recordHistory("Organization was registered", millisecondSinceEpoch));
    newOrganizationEntity.setProperty("changeHistory", changeHistory);

    //TODO use UserId's here
    ArrayList<String> moderatorList = new ArrayList<String>();
    moderatorList.add(user.getUserId());

    newOrganizationEntity.setProperty("creationTimeStampMillis", millisecondSinceEpoch);
    newOrganizationEntity.setProperty("lastEditTimeStampMillis", millisecondSinceEpoch);
    newOrganizationEntity.setProperty("isApproved", false);
    newOrganizationEntity.setProperty("moderatorList", moderatorList);
    newOrganizationEntity.setProperty("changeHistory", changeHistory);

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
