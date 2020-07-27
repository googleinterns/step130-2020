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
    //                  Existing entity table
    // +-------+-------------------------+------------------+---------+
    // | Index | creationTimeStampMillis | eventOwnerOrgIds | zipCode |
    // +-------+-------------------------+------------------+---------+
    // |     0 |                       0 | 1, 3, 7          |   12345 |
    // |     1 |                       1 | 3, 9, 4          |   02763 |
    // |     2 |                       2 | 0, 2, 5          |   47906 |
    // |     3 |                       3 | 4                |   47906 |
    // |     4 |                       4 | 5, 8, 0          |   02763 |
    // |     5 |                       5 | 1, 4             |   02763 |
    // |     6 |                       6 | 6, 3             |   94566 |
    // +-------+-------------------------+------------------+---------+

    Entity entity0 = new Entity("Event");
    entity0.setProperty("creationTimeStampMillis", 0);
    entity0.setProperty("eventOwnerOrgIds", new ArrayList<Long>(Arrays.asList(new Long(1), new Long(3), new Long(7))));
    entity0.setProperty("eventZipCode", "12345");
    masterEntityList.add(entity0);
    datastore.put(entity0);
 
    Entity entity1 = new Entity("Event");
    entity1.setProperty("creationTimeStampMillis", 1);
    entity1.setProperty("eventOwnerOrgIds", new ArrayList<Long>(Arrays.asList(new Long(3), new Long(9), new Long(4))));
    entity1.setProperty("eventZipCode", "02763");
    masterEntityList.add(entity1);
    datastore.put(entity1);
 
    Entity entity2 = new Entity("Event");
    entity2.setProperty("creationTimeStampMillis", 2);
    entity2.setProperty("eventOwnerOrgIds", new ArrayList<Long>(Arrays.asList(new Long(0), new Long(2), new Long(5))));
    entity2.setProperty("eventZipCode", "47906");
    masterEntityList.add(entity2);
    datastore.put(entity2);
 
    Entity entity3 = new Entity("Event");
    entity3.setProperty("creationTimeStampMillis", 3);
    entity3.setProperty("eventOwnerOrgIds", new ArrayList<Long>(Arrays.asList(new Long(4))));
    entity3.setProperty("eventZipCode", "47906");
    masterEntityList.add(entity3);
    datastore.put(entity3);
 
    Entity entity4 = new Entity("Event");
    entity4.setProperty("creationTimeStampMillis", 4);
    entity4.setProperty("eventOwnerOrgIds", new ArrayList<Long>(Arrays.asList(new Long(5), new Long(8), new Long(0))));
    entity4.setProperty("eventZipCode", "02763");
    masterEntityList.add(entity4);
    datastore.put(entity4);
 
    Entity entity5 = new Entity("Event");
    entity5.setProperty("creationTimeStampMillis", 5);
    entity5.setProperty("eventOwnerOrgIds", new ArrayList<Long>(Arrays.asList(new Long(1), new Long(4))));
    entity5.setProperty("eventZipCode", "02763");
    masterEntityList.add(entity5);
    datastore.put(entity5);
 
    Entity entity6 = new Entity("Event");
    entity6.setProperty("creationTimeStampMillis", 6);
    entity6.setProperty("eventOwnerOrgIds", new ArrayList<Long>(Arrays.asList(new Long(6), new Long(3))));
    entity6.setProperty("eventZipCode", "94566");
    masterEntityList.add(entity6);
    datastore.put(entity6);
  }
 
  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testEventQueryForUserOrgs() {
    GivrUser mockUser = mock(GivrUser.class);
    
    ArrayList<Entity> moderatingOrgs = new ArrayList<Entity>();
    /* This user moderates the orgs w/ ID's 1 and 3 */
    moderatingOrgs.add(new Entity("Distributor", 1));
    moderatingOrgs.add(new Entity("Distributor", 3));
    when(mockUser.getModeratingOrgs()).thenReturn(moderatingOrgs);
    
    /* Expected list only contains events hosted by orgs 1 & 3 */
    ArrayList<Entity> expectedList = new ArrayList<Entity>();
    expectedList.add(masterEntityList.get(6));
    expectedList.add(masterEntityList.get(5));
    expectedList.add(masterEntityList.get(1));
    expectedList.add(masterEntityList.get(0));

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getParameter("zipcode")).thenReturn(null);
    when(mockRequest.getParameter("displayForUser")).thenReturn("true");
    when(mockRequest.getParameter("displayForUser")).thenReturn("true");
 
    Query receivedQuery = ListHelper.getQuery("Event", mockRequest, mockUser);
 
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    Assert.assertArrayEquals(expectedList.toArray(), datastore.prepare(receivedQuery).asList(fetchOptions).toArray());
  }
}