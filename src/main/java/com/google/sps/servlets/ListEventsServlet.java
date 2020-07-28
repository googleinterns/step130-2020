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
import java.util.HashMap;
import java.util.Map;
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
import com.google.sps.data.Event;
import com.google.sps.data.ListHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.GivrUser;
import java.io.IOException;

@WebServlet("/list-events")
public class ListEventsServlet extends HttpServlet {

  /* Events & orgs have different labels for fields (ex: orgCity vs eventCity). This map is used to reference
   * the event-specific fields easily from ListHelper */
  public static HashMap<String, String> constantMap = new HashMap<String, String>();
  static {
    constantMap.put("name", "eventTitle");
    constantMap.put("streetAddress", "eventStreetAddress");
    constantMap.put("zipcode", "eventZipCode");
  }

  /*
   * This get request returns a list of events depending on its query parameters. 
   * If no parameters are included, it will return a default list of events
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    GivrUser currentUser = GivrUser.getCurrentLoggedInUser();

    /* All get requests will return a maximum of 5 events entities */
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(5);

    String startCursor = request.getParameter("cursor");
    if ((startCursor != null) && (!startCursor.equals("none"))) { //if the given cursor is 'none' no cursor is necessary
      fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
    }

    Query query = ListHelper.getQuery("Event", request, currentUser);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery prepQuery = datastore.prepare(query);
    
    QueryResultList<Entity> results = prepQuery.asQueryResultList(fetchOptions);

    Cursor endCursor = results.getCursor();
    String encodedEndCursor = endCursor.toWebSafeString();

    ArrayList<Event> requestedEvents = new ArrayList<Event>();

    /* Fills requestedEvents array*/
    for (Entity entity : results) {
      Event newEvent = new Event(entity);
      requestedEvents.add(newEvent);
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(requestedEvents));
    response.addHeader("Cursor", encodedEndCursor);
  }
}
