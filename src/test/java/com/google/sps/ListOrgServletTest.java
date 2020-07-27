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
import com.google.sps.data.ListHelper;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
 
/** */
@RunWith(JUnit4.class)
public final class ListOrgServletTest {
 
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
  /* The masterEntityList is a list of entities that are populated into the local datastore, and also exist 
   * in an arraylist. For each test, the datastore applies the Query received from the servlet, and then
   * compares that query to a hardcoded list of expected entities given that tests parameters / user fields */
  private ArrayList<Entity> masterEntityList = new ArrayList<>();
 
  @Before
  public void setUp() {
    helper.setUp();
    //                  Existing entity table
    // +-------+-------------------------+------------+---------+
    // | Index | creationTimeStampMillis | isApproved | zipCode |
    // +-------+-------------------------+------------+---------+
    // |     0 |                       0 | false      |   12345 |
    // |     1 |                       1 | true       |   02763 |
    // |     2 |                       2 | true       |   47906 |
    // |     3 |                       3 | false      |   47906 |
    // |     4 |                       4 | true       |   02763 |
    // |     5 |                       5 | false      |   02763 |
    // |     6 |                       6 | false      |   94566 |
    // +-------+-------------------------+------------+---------+
 
    Entity entity0 = new Entity("Distributor");
    entity0.setProperty("creationTimeStampMillis", 0);
    entity0.setProperty("isApproved", false);
    entity0.setProperty("orgZipCode", "12345");
    masterEntityList.add(entity0);
    datastore.put(entity0);
 
    Entity entity1 = new Entity("Distributor");
    entity1.setProperty("creationTimeStampMillis", 1);
    entity1.setProperty("isApproved", true);
    entity1.setProperty("orgZipCode", "02763");
    masterEntityList.add(entity1);
    datastore.put(entity1);
 
    Entity entity2 = new Entity("Distributor");
    entity2.setProperty("creationTimeStampMillis", 2);
    entity2.setProperty("isApproved", true);
    entity2.setProperty("orgZipCode", "47906");
    masterEntityList.add(entity2);
    datastore.put(entity2);
 
    Entity entity3 = new Entity("Distributor");
    entity3.setProperty("creationTimeStampMillis", 3);
    entity3.setProperty("isApproved", false);
    entity3.setProperty("orgZipCode", "47906");
    masterEntityList.add(entity3);
    datastore.put(entity3);
 
    Entity entity4 = new Entity("Distributor");
    entity4.setProperty("creationTimeStampMillis", 4);
    entity4.setProperty("isApproved", true);
    entity4.setProperty("orgZipCode", "02763");
    masterEntityList.add(entity4);
    datastore.put(entity4);
 
    Entity entity5 = new Entity("Distributor");
    entity5.setProperty("creationTimeStampMillis", 5);
    entity5.setProperty("isApproved", false);
    entity5.setProperty("orgZipCode", "02763");
    masterEntityList.add(entity5);
    datastore.put(entity5);
 
    Entity entity6 = new Entity("Distributor");
    entity6.setProperty("creationTimeStampMillis", 6);
    entity6.setProperty("isApproved", false);
    entity6.setProperty("orgZipCode", "94566");
    masterEntityList.add(entity6);
    datastore.put(entity6);
  }
 
  @After
  public void tearDown() {
    helper.tearDown();
  }
 
  @Test
  public void testQueryWithNoFilter() {
    /* Because there are no filters, all entities are returned in reverse time order. */
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
    expectedList.add(masterEntityList.get(6));
    expectedList.add(masterEntityList.get(5));
    expectedList.add(masterEntityList.get(4));
    expectedList.add(masterEntityList.get(3));
    expectedList.add(masterEntityList.get(2));
    expectedList.add(masterEntityList.get(1));
    expectedList.add(masterEntityList.get(0));
 
    /* This user is a maintainer, meaning they see everything */
    GivrUser mockUser = new GivrUser("testId", true, true, "google.com", "testemail@gmail.com");
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
  
    when(mockRequest.getParameterValues("zipcode")).thenReturn(null);
    when(mockRequest.getParameter("displayForUser")).thenReturn("false");
 
    Query receivedQuery = ListHelper.getQuery("Distributor", mockRequest, mockUser);
 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }
 
  @Test
  public void testQueryAsNormalUser() {
 
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
    /* Only the isApproved = true orgs are added to this simulated query, and they are added in
     * descending order of their timestamp */
    expectedList.add(masterEntityList.get(4));
    expectedList.add(masterEntityList.get(2));
    expectedList.add(masterEntityList.get(1));
 
    /* This user is not a maintainer, so they only see approved orgs*/
    GivrUser mockUser = new GivrUser("testId", false, true, "google.com", "testemail@gmail.com");
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
  
    when(mockRequest.getParameterValues("zipcode")).thenReturn(null);
    when(mockRequest.getParameter("displayForUser")).thenReturn("false");
 
    Query receivedQuery = ListHelper.getQuery("Distributor", mockRequest, mockUser);
 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }
 @Test
  public void testZipcodeFilter() {
 
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
    /* Only the orgs with the zipcode 02763 are added to the expected query results */
    expectedList.add(masterEntityList.get(5));
    expectedList.add(masterEntityList.get(4));
    expectedList.add(masterEntityList.get(1));
 
    GivrUser mockUser = new GivrUser("testId", true, true, "google.com", "testemail@gmail.com");
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
  
    when(mockRequest.getParameterValues("zipcode")).thenReturn(new String[]{"02763"});
    when(mockRequest.getParameter("displayForUser")).thenReturn("false");
 
    Query receivedQuery = ListHelper.getQuery("Distributor", mockRequest, mockUser);
 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
 
    Assert.assertArrayEquals(expectedList.toArray(), this.datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }
}
