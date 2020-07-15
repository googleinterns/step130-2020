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
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Organization;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.GivrUser;
import java.io.IOException;

@WebServlet("/list-organizations")
public class ListOrganizationsServlet extends HttpServlet {

  /*
   * This get request returns a list of organizations depending on its query parameters. 
   * If no parameters are included, it will return a default list of organizations
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    GivrUser currentUser = GivrUser.getLoggedInUser();

    /* All get requests will return a maximum of 5 organization entities */
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);
    Query query = getQueryFromParams(request, currentUser);
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

  /* This function constructs a query based on the request parameters & user's role */
  public static Query getQueryFromParams(HttpServletRequest request, GivrUser currentUser) {
    Query query = new Query("Distributor").addSort("creationTimeStampMillis", SortDirection.DESCENDING);

    ArrayList filterCollection = new ArrayList<>();

    String zipcode  = "";
    boolean queryForZipcode = false;
    if (request.getParameter("zipcode") != null) {
      zipcode = request.getParameter("zipcode");
      queryForZipcode = true;
    }

    ArrayList<String> filterParams = new ArrayList<String>();
    boolean queryForFilters = false;
    if (request.getParameterValues("filterParam") != null) {
      queryForFilters = true;
      Collections.addAll(filterParams, request.getParameterValues("filterParam"));
      System.out.println("-----------------------------------------------");
      for (String param : filterParams) {
        System.out.println("One of the filter params is: " + param);
      }
      System.out.println("-----------------------------------------------");
    }

    /* displayUserOrgsParameter is true when user only wants to see orgs they moderate*/
    String displayUserOrgsParameter = request.getParameter("displayUserOrgs");
    boolean displayUserOrgs = coerceParameterToBoolean(request, displayUserOrgsParameter);
    boolean isUserLoggedIn = (currentUser != null);
    // Ternary operator is used to check if userIsMaintainer to protect against null currentUser
    boolean userIsMaintainer = isUserLoggedIn ? currentUser.isMaintainer() : false;

    if (queryForZipcode) {
      filterCollection.add(new FilterPredicate("orgZipCode", FilterOperator.EQUAL, zipcode));
    }

    if (isUserLoggedIn && displayUserOrgs) {
      /* If the user is logged in and wants to just see their orgs, get their user ID & index with it*/
      String userId = currentUser.getUserId();
      filterCollection.add(new FilterPredicate("moderatorList", FilterOperator.EQUAL, userId));
    }

    if (!userIsMaintainer) {
      /* If the user is not a maintainer, only allow them to see approved orgs */
      filterCollection.add(new FilterPredicate("isApproved", FilterOperator.EQUAL, true));
    }

    if (queryForFilters) {
      for (String filterParam : filterParams) {
        ArrayList individualFilterCollection = new ArrayList<>();
        /* These filters are joined by an OR operator, so if any of them yield a match it will add that entity to the query */
        individualFilterCollection.add(new FilterPredicate("orgName", FilterOperator.EQUAL, filterParam));
        individualFilterCollection.add(new FilterPredicate("orgPhoneNum", FilterOperator.EQUAL, filterParam));
        individualFilterCollection.add(new FilterPredicate("orgStreetAddress", FilterOperator.EQUAL, filterParam));
        individualFilterCollection.add(new FilterPredicate("resourceCategories", FilterOperator.EQUAL, filterParam));
        //TODO: Add a "city" field to the address area. right now you can only query by org address if it is a direct match

        /* Creates a composite OR filter for each filterParam */
        CompositeFilter compositeORFilter = new CompositeFilter(CompositeFilterOperator.OR, individualFilterCollection);
        filterCollection.add(compositeORFilter); // This filter collection can contain filter AND composite filter types
      }
    }

    if (filterCollection.size() >= 2) {
      /* Composite Filter only works with 2 or more filters. */
      System.out.println("DOING THIS FILTER");
      CompositeFilter combinedQueryFilter = new CompositeFilter(CompositeFilterOperator.AND, filterCollection);
      query.setFilter(combinedQueryFilter);
    } else if (filterCollection.size() == 1) {
      /* If a filter exists but it can't be composite, normal one is applied */
      System.out.println("DOING THIS ONE INSTEAD");
      query.setFilter((Filter) filterCollection.get(0));
    }
    return query;
  }

  public static boolean coerceParameterToBoolean(HttpServletRequest request, String key) {
    String requestParameter = request.getParameter(key);
    return (requestParameter != null) && (requestParameter.equals("true"));
  }
}
