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

  private LocalServiceTestHelper helper;
  private ArrayList<Entity> listOfEntities = new ArrayList<>();

  private DatastoreService addEntitiesAndGetDatastore() {
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

    return datastore;
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void getUserFromDatastoreWithPropertyTest() {
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    helper.setUp();

    DatastoreService datastore = addEntitiesAndGetDatastore();

    String propertyName = "userEmail";
    String propertyValue = "jennb206+test1@gmail.com";

    Entity actualEntity = GivrUser.getUserFromDatastoreWithProperty(propertyName, propertyValue);
    Entity expectedEntity = listOfEntities.get(1);

    Assert.assertEquals(expectedEntity, actualEntity);
  }

  @Test
  public void getCurrentLoggedInUserTestWhenNotLoggedIn() {
    helper = new LocalServiceTestHelper(new LocalUserServiceTestConfig());
    helper.setEnvIsLoggedIn(false);
    helper.setUp();

    GivrUser actualUser = GivrUser.getCurrentLoggedInUser();
    GivrUser expectedUser = new GivrUser("", false, false, "logoutLinkHere.com", "");

    Assert.assertEquals(expectedUser, actualUser);
  }

  @Test
  public void getCurrentLoggedInUserTestWhenLoggedIn() {
    LocalUserServiceTestConfig userServiceConfig = new LocalUserServiceTestConfig();
    userServiceConfig.setOAuthEmail("jennb206+test0@gmail.com");
    userServiceConfig.setOAuthUserId("User0");
    userServiceConfig.setOAuthAuthDomain("gmail.com");

    LocalDatastoreServiceTestConfig datastoreConfig = new LocalDatastoreServiceTestConfig();

    helper = new LocalServiceTestHelper(userServiceConfig, datastoreConfig);
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail("jennb206+test0@gmail.com");
    helper.setEnvAuthDomain("gmail.com");

    Map<String,Object> envAttributeMap = new HashMap<String,Object>();
    envAttributeMap.put("com.google.appengine.api.users.UserService.user_id_key", "User0");
    helper.setEnvAttributes(envAttributeMap);
    helper.setUp();

    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = addEntitiesAndGetDatastore();

    String expectedUserId = "User0";
    boolean expectedUserIsMaintainer = true;
    boolean expectedUserIsLoggedIn = true;
    String expectedUserLoginURL = ""; // Does not need loginURL when user is logged in.
    String expectedUserEmail = "jennb206+test0@gmail.com";

    GivrUser expectedUser = new GivrUser(expectedUserId, expectedUserIsMaintainer, expectedUserIsLoggedIn, expectedUserLoginURL, expectedUserEmail);
    GivrUser actualUser = GivrUser.getCurrentLoggedInUser();

    Assert.assertEquals(expectedUser, actualUser);
  }
}
