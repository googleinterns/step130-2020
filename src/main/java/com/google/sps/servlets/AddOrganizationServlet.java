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
import java.time.Instant;
import java.io.IOException;

@WebServlet("/add-organization")
public class AddOrganizationServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // for now, ID will just be the one datastore gives it automatically
    String orgName = request.getParameter("org-name");
    String orgEmail = request.getParameter("email");
    String orgStreetAddress = request.getParameter("address");
    String orgPhoneNum = request.getParameter("phone-number");
    String orgUrl = request.getParameter("url-link");
    String orgDescription = request.getParameter("description");
    String hourOpen = request.getParameter("hour-open");
    String hourClosed = request.getParameter("hour-closed");

    /* MillisecondSinceEpoch represent the number of milliseconds that have passed since
     * 00:00:00 UTC on January 1, 1970. It ensures that all users are entering a representation
     * of time that is independent of their time zone */
    long millisecondSinceEpoch = Instant.now().toEpochMilli();

    // when suppliers are added, Entity kind will be from a parameter- for now is hardcoded
    Entity newOrganization = new Entity("Distributor");

    // for now just taking open & closed hours from form, not checking for null or that closed time > open

    // This implementation has 0 = 12:00AM, 1 = 1:00AM, 13 = 1:00PM, etc. 
    ArrayList<Integer> openHours = new ArrayList<Integer>();
    openHours.add(Integer.parseInt(hourOpen));
    openHours.add(Integer.parseInt(hourClosed));

    /* This implementation stores history entries as embedded entities instead of custom objects
     * because it is much simpler that way */
    ArrayList changeHistory = new ArrayList<>();
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    String username;
    if (isUserLoggedIn) {
      /* Currently uses user email to be consistent w other parts of codebase, subject to change */
      username = userService.getCurrentUser().getEmail();
    } else {
      username = "Unknown Author";
    }
    
    EmbeddedEntity historyEntry = new EmbeddedEntity();

    historyEntry.setProperty("changeAuthor", username);
    historyEntry.setProperty("changeMessage", "Organization was registered");
    historyEntry.setProperty("changeTimeStamp", millisecondSinceEpoch);
    changeHistory.add(historyEntry);

    ArrayList<String> moderatorList = new ArrayList<String>();
    moderatorList.add(username);

    newOrganization.setProperty("creationTimeStamp", millisecondSinceEpoch);
    newOrganization.setProperty("lastEditTimeStamp", millisecondSinceEpoch);
    newOrganization.setProperty("orgName", orgName);
    newOrganization.setProperty("orgEmail", orgEmail);
    newOrganization.setProperty("orgPhoneNum", orgPhoneNum);
    newOrganization.setProperty("orgStreetAddress", orgStreetAddress);
    newOrganization.setProperty("orgDescription", orgDescription);
    newOrganization.setProperty("orgWebsite", orgUrl);
    newOrganization.setProperty("openHours", openHours);
    newOrganization.setProperty("isApproved", false);
    newOrganization.setProperty("moderatorList", moderatorList);
    newOrganization.setProperty("changeHistory", changeHistory);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newOrganization);

    response.sendRedirect("/index.html");
  }
}
