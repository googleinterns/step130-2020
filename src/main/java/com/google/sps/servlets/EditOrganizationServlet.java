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
import java.util.ArrayList;
import java.util.Arrays;
import com.google.sps.data.User;
import java.io.IOException;
import com.google.gson.Gson;

@WebServlet("/edit-organization")
public class EditOrganizationServlet extends HttpServlet {
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    //Get the proper organization entity for updating
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    long id = Long.parseLong(request.getParameter("id"));
    Key organizationKey = KeyFactory.createKey("Distributor", id);
    Entity organization = new Entity("Distributor");

    // try catch for compliation purposes, servlet will not be called without a valid id param
    try {
      organization = datastore.get(organizationKey);
    } catch(com.google.appengine.api.datastore.EntityNotFoundException err) {
        System.out.println("Entity Not Found");
    }

    // Get all information from form
    String newOrgName = request.getParameter("org-name");
    String newOrgEmail = request.getParameter("email");
    String newOrgStreetAddress = request.getParameter("address");
    String newOrgPhoneNum = request.getParameter("phone-number");
    String newOrgUrl = request.getParameter("url-link");
    String newOrgDescription = request.getParameter("description");
    String newHourOpen = request.getParameter("hour-open");
    String newHourClosed = request.getParameter("hour-closed");
    String newApproval = request.getParameter("approval");
    String newModeratorList = request.getParameter("moderator-list");

    //TODO: get timestamp with transactions instead
    long timestampMillis = System.currentTimeMillis();

    // Set organization properties with inputted form information
    organization.setProperty("orgName", newOrgName);
    organization.setProperty("orgEmail", newOrgEmail);
    organization.setProperty("orgStreetAddress", newOrgStreetAddress);
    organization.setProperty("orgPhoneNum", newOrgPhoneNum);
    organization.setProperty("orgUrl", newOrgUrl);
    organization.setProperty("orgDescription", newOrgDescription);
    organization.setProperty("lastEditTimeStamp", timestampMillis);

    // Will only be changed by maintainers
    if(newApproval.equals("approved")) {
        organization.setProperty("isApproved", true);
    } else if(newApproval.equals("notApproved")) {
        organization.setProperty("isApproved", false);
    }


    // TODO: make a better way or storing this data (class?)
    ArrayList<Integer> openHours = new ArrayList<Integer>();
    openHours.add(Integer.parseInt(newHourOpen));
    openHours.add(Integer.parseInt(newHourClosed));
    organization.setProperty("openHours", openHours);

    // Will split the list using the delimitter zero or more whitespace, comma, zero or more whitespace
    ArrayList<String> moderatorList = new ArrayList<String>(Arrays.asList(newModeratorList.split("\\s*,\\s*")));
    organization.setProperty("moderatorList", moderatorList);

    // // TODO: change this to our History object- for prototyping just using strings
    ArrayList<String> changeHistory = (ArrayList) organization.getProperty("changeHistory");
    changeHistory.add("Organization was was edited at " + timestampMillis);
    organization.setProperty("changeHistory", changeHistory);

  
    datastore.put(organization);

    System.out.println("Edited Organization");
    response.sendRedirect("/index.html");
  }
}
