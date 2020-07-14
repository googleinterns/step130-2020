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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.GivrUser;
import com.google.sps.servlets.AddMaintainerServlet.java;
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

/** */
@RunWith(JUnit4.class)
public final class AddMaintainerServletTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Mock
  HttpServletRequest mockRequest;

  @Mock
  HttpServletResponse mockResponse;

  @Before
  public void setUp() {
    helper.setUp();

    MockitoAnnotations.initMocks(this);

    Entity user0 = new Entity("User");
    user0.setProperty("userId", "");
    user0.setProperty("isMaintainer", true);
    user0.setProperty("userEmail", "jennb206+test0@gmail.com");
    datastore.put(user0);

    Entity user1 = new Entity("User");
    user1.setProperty("userId", "");
    user1.setProperty("isMaintainer", true);
    user1.setProperty("userEmail", "jennb206+test1@gmail.com");
    datastore.put(user1);
    
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
  * When AddMaintainerServlet is called by a non-Maintainer, servlet should throw an error.
  */
  @Test
  public void Add_InvokedByNonMaintainer_ShouldSendError() {
    mockRequest = mock(HttpServletRequest.class);
    GivrUser mockCurrentUser = new GivrUser("testId", false, true, "", "jennb206+mockCurrentUser@gmail.com");
    
    when(mockRequest.getParameter("userEmail")).thenReturn("jennb206+test0@gmail.com");
    when(GivrUser.getCurrentLoggedInUser()).thenReturn(mockCurrentUser);

    AddMaintainerServlet servlet = new AddMaintainerServlet();
    servlet.doPost(mockRequest, mockResponse);

    int actualResponseStatus = mockResponse.getStatus();
    int expectedResponseStatus = HttpServletResponse.SC_NOT_FOUND;

    Assert.assertEquals(expectedResponseStatus, actualResponseStatus);
  }

  // @Test
  // public void testRequestReturnsCorrect() {
    

  // }
      
}
