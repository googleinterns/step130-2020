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
import java.util.Map;
import java.util.HashMap;
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
import com.google.gson.Gson;

/** */
@RunWith(JUnit4.class)
public final class GivrUserTest {

  // private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig(), new LocalDatastoreServiceTestConfig());
  private LocalServiceTestHelper helper;

  // private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  // private UserService userService = UserServiceFactory.getUserService();

  private ArrayList<Entity> listOfEntities = new ArrayList<>();

  // @Before
  // public void setUp() {
  //   helper.setUp();

    //                  Existing User entity table
    // +-------+--------+--------------+--------------------------+
    // | Index | userId | isMaintainer |         userEmail        |
    // +-------+-----------------------+--------------------------+
    // |     0 |      0 |         true | jennb206+test0@gmail.com |
    // |     1 |      1 |        false | jennb206+test1@gmail.com |
    // |     2 |      2 |         true | jennb206+test2@gmail.com |
    // |     3 |      3 |        false | jennb206+test3@gmail.com |
    // +-------+--------+--------------+--------------------------+

    /*
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
    listOfEntities.add(user1); */
  // }

  // @After
  // public void tearDown() {
  //   helper.tearDown();
  // }

  // @Test
  // public void getUserFromDatastoreWithPropertyTest() {
  //   helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  //   helper.setUp();
  //   Entity user0 = new Entity("User");
  //   user0.setProperty("userId", "User0");
  //   user0.setProperty("isMaintainer", true);
  //   user0.setProperty("userEmail", "jennb206+test0@gmail.com");
  //   datastore.put(user0);
  //   listOfEntities.add(user0);

  //   Entity user1 = new Entity("User");
  //   user1.setProperty("userId", "User1");
  //   user1.setProperty("isMaintainer", false);
  //   user1.setProperty("userEmail", "jennb206+test1@gmail.com");
  //   datastore.put(user1);
  //   listOfEntities.add(user1);
  //   String propertyName = "userEmail";
  //   String propertyValue = "jennb206+test1@gmail.com";

  //   Entity actualEntity = GivrUser.getUserFromDatastoreWithProperty(propertyName, propertyValue);

  //   Entity expectedEntity = listOfEntities.get(1);

  //   Assert.assertEquals(expectedEntity, actualEntity);
  // }

  // @Test
  // public void getCurrentLoggedInUserTestWhenNotLoggedIn() {
  //   helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
  //   helper.setEnvIsLoggedIn(false);
  //   helper.setUp();
  //   GivrUser actualUser = GivrUser.getCurrentLoggedInUser();
  //   GivrUser expectedUser = new GivrUser("", false, false, "logoutLinkHere.com", "");

  //   Assert.assertEquals(expectedUser, actualUser);
  // }

  @Test
  public void getCurrentLoggedInUserTestWhenLoggedIn() {
    LocalUserServiceTestConfig userServiceConfig = new LocalUserServiceTestConfig();
    userServiceConfig.setOAuthEmail("jennb206+test0@gmail.com");
    userServiceConfig.setOAuthUserId("User0");
    userServiceConfig.setOAuthAuthDomain("gmail.com");
    // userServiceConfig.setUp();

    LocalDatastoreServiceTestConfig datastoreConfig = new LocalDatastoreServiceTestConfig();
    // datastoreConfig.setUp();

    helper = new LocalServiceTestHelper(userServiceConfig, datastoreConfig);
    


    helper.setEnvIsLoggedIn(true);

    helper.setEnvEmail("jennb206+test0@gmail.com");
    helper.setEnvAuthDomain("gmail.com");

    Map<String,Object> map = new HashMap<String,Object>();

    map.put("USER_EMAIL", "foo@gmail.com");
    map.put("USER_ID", "1");
    helper.setEnvAttributes(map); //userEmail, userId - takes in Map <String, String>
    helper.setUp();

    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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

    System.out.println("hello "  + userService.isUserLoggedIn() + " " + userService.getCurrentUser().getUserId());

    GivrUser actualUser = GivrUser.getCurrentLoggedInUser();
    GivrUser expectedUser = new GivrUser("User0", true, true, "", "jennb206+test0@gmail.com");

    Gson gson = new Gson();
    String json1 = gson.toJson(actualUser);
    String json2 = gson.toJson(expectedUser);

    System.out.println("!!!!\n" + json1 + "\n" + json2);

    if ( expectedUser.equals(actualUser)) {
      System.out.println("Users equal each other");
    } else {
      System.out.println("Users do not equal each other");
    }
    Assert.assertEquals(expectedUser, actualUser); 
    //PROBLEM here, actual user is returning wrong info
  }
}