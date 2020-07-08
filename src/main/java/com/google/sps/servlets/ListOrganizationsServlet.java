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
import com.google.gson.Gson;
import java.util.ArrayList;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Organization;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.User;
import java.io.IOException;

@WebServlet("/list-organizations")
public class ListOrganizationsServlet extends HttpServlet {

  /*
   * This get request returns a list of organizations depending on its query parameters. 
   * If no parameters are included, it will return a default list of organizations
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /* All get requests will return a maximum of 5 organization entities */
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);
    
    /* TODO: Implement using pagination cursors- first implementation will just return 5 first entries */

    /* This parameter is used to tell the servlet whether the user is requesting to see just the orgs they moderate*/
    String displayUserOrgsParameter = request.getParameter("displayUserOrgs");
    boolean displayUserOrgs = false;

    /* If a parameter was sent & is set to 'true', then the displayUserOrgs boolean changes to true */
    if ((displayUserOrgsParameter != null) && (displayUserOrgsParameter.equals("true"))) {
      displayUserOrgs = true;
    }

    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();

    Query query;

    if (isUserLoggedIn && displayUserOrgs) {
      /* If the user is logged in and wants to just see their orgs, get their user ID & index with it*/
      String userId = userService.getCurrentUser().getUserId();
      query = ConstructQueryForUserInfo(userId);
    } else {
      /* If no username was included, it just returns all orgs */
        // TODO: make this only return approved orgs
      query = new Query("Distributor").addSort("creationTimeStampMillis", SortDirection.DESCENDING);
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepQuery = datastore.prepare(query);
    
    QueryResultList<Entity> results = prepQuery.asQueryResultList(fetchOptions);
    ArrayList<Organization> requestedOrganizations = new ArrayList<Organization>();

    /* Fills requestedOrganizations array*/
    for (Entity entity : results) {
      
      // TODO(): Implement better schema to represent opening and closing hours for different days
      Organization newOrg = new Organization(entity);
      requestedOrganizations.add(newOrg);
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(requestedOrganizations));
  }

  public Query ConstructQueryForUserInfo(String userID) {
    Query query = new Query("Distributor").setFilter(new FilterPredicate("moderatorList",
                    FilterOperator.EQUAL, userID)).addSort("creationTimeStampMillis", SortDirection.DESCENDING);
    return query;
  }
}
