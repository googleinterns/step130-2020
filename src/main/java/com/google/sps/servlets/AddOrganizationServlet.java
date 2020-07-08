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
import com.google.sps.data.GivrUser;
import java.text.SimpleDateFormat;
import com.google.sps.data.GivrUser;
import com.google.sps.data.HistoryManager;
import java.time.Instant;
import java.io.IOException;

@WebServlet("/add-organization")
public class AddOrganizationServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    String username;
    String userId;
    if (isUserLoggedIn) {
      /* Currently uses user email to be consistent w other parts of codebase, subject to change */
      username = userService.getCurrentUser().getEmail();
      userId = userService.getCurrentUser().getUserId();
    } else {
      throw new IllegalArgumentException("Error: unable to register organization if user is not logged in.");
    }
    
    // for now, ID will just be the one datastore gives it automatically
    String orgName = request.getParameter("org-name");
    String orgEmail = request.getParameter("email");
    String orgStreetAddress = request.getParameter("address");
    String orgPhoneNum = request.getParameter("phone-number");
    String orgUrl = request.getParameter("url-link");
    String orgDescription = request.getParameter("description");

    /* MillisecondSinceEpoch represent the number of milliseconds that have passed since
     * 00:00:00 UTC on January 1, 1970. It ensures that all users are entering a representation
     * of time that is independent of their time zone */
    long millisecondSinceEpoch = Instant.now().toEpochMilli();

    // when suppliers are added, Entity kind will be from a parameter- for now is hardcoded
    Entity newOrganization = new Entity("Distributor");

    /* This implementation stores history entries as embedded entities instead of custom objects
     * because it is much simpler that way */
    ArrayList changeHistory = new ArrayList<>();
    
    HistoryManager history = new HistoryManager();

    changeHistory.add(history.recordHistory("Organization was registered", millisecondSinceEpoch));
    newOrganization.setProperty("changeHistory", changeHistory);

    //TODO use UserId's here
    ArrayList<String> moderatorList = new ArrayList<String>();
    moderatorList.add(username);

    //TODO: Use sarah's code to get these fields directly from the request
    newOrganization.setProperty("creationTimeStampMillis", millisecondSinceEpoch);
    newOrganization.setProperty("lastEditTimeStampMillis", millisecondSinceEpoch);
    newOrganization.setProperty("orgName", orgName);
    newOrganization.setProperty("orgEmail", orgEmail);
    newOrganization.setProperty("orgPhoneNum", orgPhoneNum);
    newOrganization.setProperty("orgStreetAddress", orgStreetAddress);
    newOrganization.setProperty("orgDescription", orgDescription);
    newOrganization.setProperty("orgWebsite", orgUrl);
    newOrganization.setProperty("isApproved", false);
    newOrganization.setProperty("moderatorList", moderatorList);
    newOrganization.setProperty("changeHistory", changeHistory);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newOrganization);

    response.sendRedirect("/index.html");
  }
}
