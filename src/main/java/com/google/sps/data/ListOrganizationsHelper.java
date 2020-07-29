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

package com.google.sps.data;

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
import javax.servlet.http.HttpServletRequest;
import com.google.sps.data.GivrUser;
import com.google.sps.servlets.ListOrganizationsServlet;
import com.google.sps.data.ListHelper;
import java.io.IOException;

public class ListOrganizationsHelper extends ListHelper {

  public ListOrganizationsHelper(String entityKind, HttpServletRequest request, GivrUser currentUser) {
      super(entityKind, request, currentUser);
  }

  /* Events & orgs have different labels for fields (ex: orgCity vs eventCity). This map is used to reference
     * the event-specific fields easily from ListHelper */
  public HashMap<String, String> GetDatastoreConstantMap() {
    HashMap<String, String> constantMap = new HashMap<String, String>();
    constantMap.put("name", "orgName");
    constantMap.put("streetAddress", "orgStreetAddress");
    constantMap.put("zipcode", "orgZipCode");
    return constantMap;
  }

  /* handleUserFiltering handles filtering related to a user's role and permissions, and whether that user has requested to only
   * see organizations or events they belong to */
  public ArrayList<Filter> handleUserFiltering(boolean displayForUser) {
    ArrayList<Filter> filterCollection = new ArrayList<Filter>();
    if (this.currentUser.isLoggedIn() && displayForUser) {
      /* If the user is logged in and wants to just see their orgs, get their user ID & index with it*/
      String userId = currentUser.getUserId();
      filterCollection.add(new FilterPredicate("moderatorList", FilterOperator.EQUAL, userId));
    }

    if (!this.currentUser.isMaintainer()) {
      /* If the user is not a maintainer, only allow them to see approved orgs */
      filterCollection.add(new FilterPredicate("isApproved", FilterOperator.EQUAL, true));
    }
    return filterCollection;
  }
}
