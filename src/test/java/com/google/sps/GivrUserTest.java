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
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.users.User;

/** */
@RunWith(JUnit4.class)
public final class GivrUserTest {

  private final LocalServiceTestHelper userServiceHelper = new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvIsAdmin(true).setEnvIsLoggedIn(true);

  private final LocalServiceTestHelper datastoreServiceHelper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private ArrayList<Entity> listOfEntities = new ArrayList<>();

  @Before
  public void setUp() {
    userServiceHelper.setUp();
    datastoreServiceHelper.setUp();

    Entity user0 = new Entity("User");
    user0.setProperty("userId", "User0");
    user0.setProperty("isMaintainer", true);
    user0.setProperty("userEmail", "jennb206+test0@gmail.com");
    datastore.put(user0);
    listOfEntities.add(user0);

    Entity user1 = new Entity("User");
    user1.setProperty("userId", "User1");
    user1.setProperty("isMaintainer", false);
    user1.setProperty("userEmail", "jennb206+test1@gmail.com");
    datastore.put(user1);
    listOfEntities.add(user1);
  }

  @After
  public void tearDown() {
    userServiceHelper.tearDown();
    datastoreServiceHelper.tearDown();
  }

  @Test
  public void getUserFromDatastoreWithPropertyTest() {
    String propertyName = "userEmail";
    String propertyValue = "jennb206+test1@gmail.com";

    Entity actualEntity = GivrUser.getUserFromDatastoreWithProperty(propertyName, propertyValue);

    Entity expectedEntity = listOfEntities.get(1);

    Assert.assertEquals(expectedEntity, actualEntity);
  }

  // @Test
  // public void getCurrentLoggedInUserTestWhenNotLoggedIn() {
  //   // UserService mockUserService = mock(UserService.class);
  //   // UserServiceFactory mockUserServiceFactory = mock(UserServiceFactory.class);

  //   // when(UserServiceFactory.getUserService()).thenReturn(userService);

  //   UserService userService = UserServiceFactory.getUserService();

  //   when(userService.isUserLoggedIn()).thenReturn(false);
  //   when(userService.createLoginURL("/")).thenReturn("loginLinkHere.com");
  //   // when(mockUserService.getCurrentUser().getUserId()).thenReturn("User0");

  //   GivrUser actualUser = GivrUser.getCurrentLoggedInUser();
  //   GivrUser expectedUser = new GivrUser("User0", false, false, "logoutLinkHere.com", "");

  //   // Assert.assertEquals(1, 0);

  //   Assert.assertEquals(expectedUser, actualUser);
  // }

  /*@Test
  public void getCurrentLoggedInUserTestWhenLoggedIn() {
    UserService mockUserService = mock(UserService.class);

    when(mockUserService.isUserLoggedIn()).thenReturn(true);

    when(mockUserService.getCurrentUser().getUserId()).thenReturn("User0");

    GivrUser actualUser = GivrUser.getCurrentLoggedInUser();
    GivrUser expectedUser = new GivrUser("User0", true, true, "", )

  }*/
}