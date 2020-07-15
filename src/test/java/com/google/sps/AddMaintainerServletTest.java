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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.GivrUser;
import com.google.sps.servlets.AddMaintainerServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;

/** */
@RunWith(JUnit4.class)
public final class AddMaintainerServletTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private ArrayList<Entity> listOfUsers = new ArrayList<>();

  @Before
  public void setUp() {
    helper.setUp();

    Entity user0 = new Entity("User");
    user0.setProperty("userId", "User0");
    user0.setProperty("isMaintainer", true);
    user0.setProperty("userEmail", "jennb206+test0@gmail.com");
    datastore.put(user0);
    listOfUsers.add(user0);

    Entity user1 = new Entity("User");
    user1.setProperty("userId", "User1");
    user1.setProperty("isMaintainer", false);
    user1.setProperty("userEmail", "jennb206+test1@gmail.com");
    datastore.put(user1);
    listOfUsers.add(user1);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
  * When AddMaintainerServlet is called by a non-Maintainer, servlet should throw an error.
  */
  // @Test
  // public void Add_InvokedByNonMaintainer_ShouldSendError() {
  //   HttpServletRequest mockRequest = mock(HttpServletRequest.class);
  //   HttpServletResponse mockResponse = mock(HttpServletResponse.class);

  //   // GivrUser mockCurrentUser = new GivrUser("testId", false, true, "", "jennb206+mockCurrentUser@gmail.com");

  //   GivrUser mockGivrUser = mock(new GivrUser("testId", false, true, "", "jennb206+mockCurrentUser@gmail.com"));
    
  //   when(mockRequest.getParameter("userEmail")).thenReturn("jennb206+test0@gmail.com");
  //   when(mockGivrUser.getCurrentLoggedInUser()).thenReturn(new GivrUser("testId", false, true, "", "jennb206+mockCurrentUser@gmail.com"));

  //   AddMaintainerServlet servlet = mock(AddMaintainerServlet.class);
  //   try {
  //     servlet.doPost(mockRequest, mockResponse);
  //   } catch(Exception exception) {

  //   }
    

  //   int actualResponseStatus = mockResponse.getStatus();
  //   int expectedResponseStatus = HttpServletResponse.SC_NOT_FOUND;

  //   Assert.assertEquals(expectedResponseStatus, actualResponseStatus);
  // }

  @Test
  public void ChangeMaintainerStatus() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    Assert.assertEquals(listOfUsers.get(1), datastore.prepare(new Query("User").setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, "User1"))).asSingleEntity());

    AddMaintainerServlet servlet = mock(AddMaintainerServlet.class);

    servlet.changeMaintainerStatus("jennb206+test1@gmail.com", "User1");

    Entity user2 = new Entity("User");
    user2.setProperty("userId", "User1");
    user2.setProperty("isMaintainer", true);
    user2.setProperty("userEmail", "jennb206+test1@gmail.com");

    //Assert.assertEquals(user2, datastore.prepare(new Query("User").setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, "User1"))).asSingleEntity());
  }
      
}
