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


package com.google.sps;

import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.GivrUser;
import com.google.sps.servlets.ListOrganizationsServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class ListOrgServletTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testQueryWithNoFilterParams() {
    /* Preparing the datastore */
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    for (int i = 0; i < 5; i++) {
      Entity mockEntity = new Entity("Distributor");
      Random rand = new Random();
      /* sets creation timestamp as random number from 1000-2000 */
      mockEntity.setProperty("creationTimeStampMillis", rand.nextInt(1000) + 1000);
      datastore.put(mockEntity);
    }

    GivrUser mockUser = new GivrUser("000", true, true, "google.com");
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
  
    when(mockRequest.getParameter("zipcode")).thenReturn(null);
    when(mockRequest.getParameter("displayUserOrgs")).thenReturn("false");

    Query receivedQuery = ListOrganizationsServlet.getQueryFromParams(mockRequest, mockUser);
    Query myQuery = new Query("Distributor").addSort("creationTimeStampMillis", SortDirection.DESCENDING);
    System.out.println("-------------------------------------------Equality evaluates to :" + datastore.prepare(receivedQuery).equals(datastore.prepare(myQuery)));

    System.out.println("RECEIVED QUERY:  " + receivedQuery.toString());
    for (Entity entity : datastore.prepare(receivedQuery).asIterable()) {
      System.out.println("ENTITY IS: " + entity.toString());
    }
    System.out.println("MY TEST QUERY:   " + myQuery.toString());
    for (Entity entity : datastore.prepare(myQuery).asIterable()) {
      System.out.println("ENTITY IS: " + entity.toString());
    }

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertIterableEquals(datastore.prepare(receivedQuery).asQueryResultList(fetchOptions), datastore.prepare(myQuery).asQueryResultList(fetchOptions));
  }
}
