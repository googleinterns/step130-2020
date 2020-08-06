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
import com.google.sps.data.ListEventsHelper;
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
public final class ListEventServletTest {
 
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
 
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
  /* The masterEntityList is a list of entities that are populated into the local datastore, and also exist 
   * in an arraylist. For each test, the datastore applies the Query received from the servlet, and then
   * compares that query to a hardcoded list of expected entities given that tests parameters / user fields */
  private ArrayList<Entity> masterEntityList = new ArrayList<>();
 
  @Before
  public void setUp() {
    helper.setUp();
    //                                Existing entity table
    // +-------+-------------------------+------------------+---------+---------------+
    // | Index | creationTimeStampMillis | ownerOrgId       | zipCode | streetAddress |
    // +-------+-------------------------+------------------+---------+---------------+
    // |     0 |                       0 | 1                |   12345 | 12 Oak st.    |
    // |     1 |                       1 | 3                |   02763 | 12 Oak st.    |
    // |     2 |                       2 | 0                |   47906 | 10 Main st.   |
    // |     3 |                       3 | 4                |   02763 | 123 Park ln.  |
    // |     4 |                       4 | 5                |   02763 | 10 Main st.   |
    // |     5 |                       5 | 1                |   02763 | 14 Oak st.    |
    // |     6 |                       6 | 6                |   94566 | 100 Birch st. |
    // +-------+-------------------------+------------------+---------+---------------+

    Entity entity0 = new Entity("Event");
    entity0.setProperty("creationTimeStampMillis", 0);
    entity0.setProperty("ownerOrgId", new Long(1));
    entity0.setProperty("zipcode", "12345");
    entity0.setProperty("streetAddress", "12 Oak st.");
    masterEntityList.add(entity0);
    datastore.put(entity0);
 
    Entity entity1 = new Entity("Event");
    entity1.setProperty("creationTimeStampMillis", 1);
    entity1.setProperty("ownerOrgId", new Long(3));
    entity1.setProperty("zipcode", "02763");
    entity1.setProperty("streetAddress", "12 Oak st.");
    masterEntityList.add(entity1);
    datastore.put(entity1);
 
    Entity entity2 = new Entity("Event");
    entity2.setProperty("creationTimeStampMillis", 2);
    entity2.setProperty("ownerOrgId", new Long(0));
    entity2.setProperty("zipcode", "47906");
    entity2.setProperty("streetAddress", "10 Main st.");
    masterEntityList.add(entity2);
    datastore.put(entity2);
 
    Entity entity3 = new Entity("Event");
    entity3.setProperty("creationTimeStampMillis", 3);
    entity3.setProperty("ownerOrgId", new Long(4));
    entity3.setProperty("zipcode", "02763");
    entity3.setProperty("streetAddress", "123 Park ln.");
    masterEntityList.add(entity3);
    datastore.put(entity3);
 
    Entity entity4 = new Entity("Event");
    entity4.setProperty("creationTimeStampMillis", 4);
    entity4.setProperty("ownerOrgId", new Long(5));
    entity4.setProperty("zipcode", "02763");
    entity4.setProperty("streetAddress", "10 Main st.");
    masterEntityList.add(entity4);
    datastore.put(entity4);
 
    Entity entity5 = new Entity("Event");
    entity5.setProperty("creationTimeStampMillis", 5);
    entity5.setProperty("ownerOrgId", new Long(1));
    entity5.setProperty("zipcode", "02763");
    entity5.setProperty("streetAddress", "14 Oak st.");
    masterEntityList.add(entity5);
    datastore.put(entity5);
 
    Entity entity6 = new Entity("Event");
    entity6.setProperty("creationTimeStampMillis", 6);
    entity6.setProperty("ownerOrgId", new Long(6));
    entity6.setProperty("zipcode", "94566");
    entity6.setProperty("streetAddress", "100 Birch st.");
    masterEntityList.add(entity6);
    datastore.put(entity6);
  }
 
  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testEventQueryForUserOrgs() {
    /* Tests to make sure that client can successfully request only user's events */
    GivrUser mockUser = mock(GivrUser.class);
    
    ArrayList<Entity> moderatingOrgs = new ArrayList<Entity>();
    /* This user moderates the orgs w/ ID's 1 and 3 */
    moderatingOrgs.add(new Entity("Distributor", 1));
    moderatingOrgs.add(new Entity("Distributor", 3));
    when(mockUser.getModeratingOrgs()).thenReturn(moderatingOrgs);

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getParameter("displayForUser")).thenReturn("true");

    /* Expected list only contains the events hosted by orgs 1 or 3 */
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
    expectedList.add(masterEntityList.get(5));
    expectedList.add(masterEntityList.get(1));
    expectedList.add(masterEntityList.get(0));
 
    ListEventsHelper listEventsHelper = new ListEventsHelper("Event", mockRequest, mockUser);
    Query receivedQuery = listEventsHelper.getQuery();
 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }

  @Test
  public void testEventQueryForFilteredUserOrgs() {
    /* Tests to make sure client can successfully see user's events with filters */
    GivrUser mockUser = mock(GivrUser.class);
    
    ArrayList<Entity> moderatingOrgs = new ArrayList<Entity>();
    /* This user moderates the orgs w/ ID's 4, 5, and 6 */
    moderatingOrgs.add(new Entity("Distributor", 4));
    moderatingOrgs.add(new Entity("Distributor", 5));
    moderatingOrgs.add(new Entity("Distributor", 6));
    when(mockUser.getModeratingOrgs()).thenReturn(moderatingOrgs);

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getParameterValues("zipcode")).thenReturn(new String[]{"02763"});
    when(mockRequest.getParameterValues("streetAddress")).thenReturn(new String[]{"10 Main st."});
    when(mockRequest.getParameter("displayForUser")).thenReturn("true");

    /* Expected list only contains the events hosted by orgs 4, 5, or 6 in zipcode 02763 w/ address 10 Main st. */
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
    expectedList.add(masterEntityList.get(4));
 
    ListEventsHelper listEventsHelper = new ListEventsHelper("Event", mockRequest, mockUser);
    Query receivedQuery = listEventsHelper.getQuery();

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }

  // TODO: add a test for when the user is a Maintainer, when the user is a moderator of an org with
  // no events, and when the user is not a moderator

  @Test
  public void testEventQueryForMaintainer() {
  /* Tests makes sure that a maintainer will see all events in the "Show My Events" tab*/
    GivrUser mockUser = mock(GivrUser.class);
    when(mockUser.isMaintainer()).thenReturn(true);

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getParameter("displayForUser")).thenReturn("true");

    /* Expected list only contains the events hosted by orgs 4, 5, or 6 in zipcode 02763 w/ address 10 Main st. */
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
    expectedList.add(masterEntityList.get(6));
    expectedList.add(masterEntityList.get(5));
    expectedList.add(masterEntityList.get(4));
    expectedList.add(masterEntityList.get(3));
    expectedList.add(masterEntityList.get(2));
    expectedList.add(masterEntityList.get(1));
    expectedList.add(masterEntityList.get(0));
 
    ListEventsHelper listEventsHelper = new ListEventsHelper("Event", mockRequest, mockUser);
    Query receivedQuery = listEventsHelper.getQuery();

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }

  @Test
  public void testEventQueryForOrgWithoutEvents() {
    /* Tests to make sure correct query is returned when user is a moderator of an org with no events*/
    GivrUser mockUser = mock(GivrUser.class);
    
    ArrayList<Entity> moderatingOrgs = new ArrayList<Entity>();
    /* This user moderates the orgs w/ ID 20, which does not have any events */
    moderatingOrgs.add(new Entity("Distributor", 20));
    when(mockUser.getModeratingOrgs()).thenReturn(moderatingOrgs);

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getParameter("displayForUser")).thenReturn("true");

    /* Expected list is empty because this user's moderating org has no events*/
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
 
    ListEventsHelper listEventsHelper = new ListEventsHelper("Event", mockRequest, mockUser);
    Query receivedQuery = listEventsHelper.getQuery();

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }

  @Test
  public void testEventQueryForNonModerator() {
    /* Tests to make sure no results are returned when displayForUser = true, but user is not moderator */
    GivrUser mockUser = mock(GivrUser.class);
    when(mockUser.isMaintainer()).thenReturn(false);

    ArrayList<Entity> moderatingOrgs = new ArrayList<Entity>();
    /* This user does not moderate any organizations */
    when(mockUser.getModeratingOrgs()).thenReturn(moderatingOrgs);

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getParameter("displayForUser")).thenReturn("true");

    /* Expected list is empty */
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
 
    ListEventsHelper listEventsHelper = new ListEventsHelper("Event", mockRequest, mockUser);
    Query receivedQuery = listEventsHelper.getQuery();

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }
}
