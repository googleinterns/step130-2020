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
import com.google.sps.data.User;
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

    //TODO: get timestamp with transactions instead
    long timestampMillis = System.currentTimeMillis();

    // when suppliers are added, Entity kind will be from a parameter- for now is hardcoded
    Entity newOrganization = new Entity("Distributor");

    // for now just taking open & closed hours from form, not checking for null or that closed time > open
    ArrayList<Integer> openHours = new ArrayList<Integer>();
    openHours.add(Integer.parseInt(hourOpen));
    openHours.add(Integer.parseInt(hourClosed));

    ArrayList<String> moderatorList = new ArrayList<String>();
    moderatorList.add("anon creator"); //dummy value for before auth gets set up

    // TODO change this to our History object- for prototyping just using strings
    ArrayList<String> changeHistory = new ArrayList<String>();
    changeHistory.add("Organization was registered");

    newOrganization.setProperty("creationTimeStamp", timestampMillis);
    newOrganization.setProperty("lastEditTimeStamp", timestampMillis);
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

    System.out.println("Added to datastore");
    response.sendRedirect("/index.html");
  }
}