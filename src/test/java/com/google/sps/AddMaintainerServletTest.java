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
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
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
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/** */
@RunWith(JUnit4.class)
public final class AddMaintainerServletTest {

  private LocalServiceTestHelper helper;

  private ArrayList<Entity> listOfUsers = new ArrayList<>();

  private DatastoreService addEntitiesAndGetDatastore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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

    Entity user2 = new Entity("User");
    user2.setProperty("userId", "User2");
    user2.setProperty("isMaintainer", false);
    user2.setProperty("userEmail", "jennb206+test2@gmail.com");
    datastore.put(user2);
    listOfUsers.add(user2);

    return datastore;
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
   * Method used in test to check if entities equal each other by values.
   */
  public boolean checkIfEntitiesEqualEachOther(Entity firstUserEntity, Entity secondUserEntity) {
    Set<String> propertySet = new HashSet<String>();
    propertySet.add("userId");
    propertySet.add("isMaintainer");
    propertySet.add("userEmail");

    for (String property: propertySet) {
      if (!firstUserEntity.getProperty(property).equals(secondUserEntity.getProperty(property))) {
        return false;
      }
    }
    return true;
  }

  /**
  * When AddMaintainerServlet is called by a non-Maintainer, servlet should throw an error.
  */
  @Test
  public void Add_InvokedByNonMaintainer_ShouldSendError() {
    LocalUserServiceTestConfig userServiceConfig = new LocalUserServiceTestConfig();
    userServiceConfig.setOAuthEmail("jennb206+test1@gmail.com");
    userServiceConfig.setOAuthUserId("User1");
    userServiceConfig.setOAuthAuthDomain("gmail.com");

    LocalDatastoreServiceTestConfig datastoreConfig = new LocalDatastoreServiceTestConfig();

    helper = new LocalServiceTestHelper(userServiceConfig, datastoreConfig);

    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail("jennb206+test1@gmail.com");
    helper.setEnvAuthDomain("gmail.com");

    Map<String,Object> envAttributeMap = new HashMap<String,Object>();
    envAttributeMap.put("com.google.appengine.api.users.UserService.user_id_key", "User1");
    helper.setEnvAttributes(envAttributeMap);
    helper.setUp();

    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    
    when(mockRequest.getParameter("userEmail")).thenReturn("jennb206+test2@gmail.com");

    Logger logger = Logger.getLogger("AddMaintainerServlet Test Logger");

    AddMaintainerServlet servlet = new AddMaintainerServlet();
    try {
      servlet.doPost(mockRequest, mockResponse);
    } catch(IOException exception) {
      logger.log(Level.SEVERE, "The doPost method in AddMaintainerServlet has failed.");
    }

    try {
      verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "The servlet failed to send error when logged in user is not a maintainer.");
    }
  }

  /**
   * Tests method addNewMaintainerToDatastore by checking number of entities within Datastore.
   */
  @Test
  public void addNewMaintainerToDatastoreTest() {
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    helper.setUp();

    DatastoreService datastore = addEntitiesAndGetDatastore();
    AddMaintainerServlet servlet = new AddMaintainerServlet();

    int expectedNumberOfEntities = 3;
    Assert.assertEquals(expectedNumberOfEntities, datastore.prepare(new Query("User")).countEntities(withLimit(10)));

    servlet.addNewMaintainerToDatastore("jennb206+test2@gmail.com");

    int actualNumberOfEntities = expectedNumberOfEntities + 1;
    Assert.assertEquals(actualNumberOfEntities, datastore.prepare(new Query("User")).countEntities(withLimit(10)));
  }

  /**
   * Tests method changeMaintainerStatus() to check if maintainer status is updated correctly in the Datastore User table.
   */
  @Test
  public void changeMaintainerStatusTest() {
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    helper.setUp();

    DatastoreService datastore = addEntitiesAndGetDatastore();
    AddMaintainerServlet servlet = new AddMaintainerServlet();

    servlet.changeMaintainerStatus("jennb206+test1@gmail.com");

    Entity expectedUserEntity = new Entity("User");
    expectedUserEntity.setProperty("userId", "User1");
    expectedUserEntity.setProperty("isMaintainer", true);
    expectedUserEntity.setProperty("userEmail", "jennb206+test1@gmail.com");

    Entity actualUserEntity = datastore.prepare(new Query("User").setFilter(new FilterPredicate("userEmail", FilterOperator.EQUAL, "jennb206+test1@gmail.com"))).asSingleEntity();

    Assert.assertTrue(checkIfEntitiesEqualEachOther(expectedUserEntity, actualUserEntity));
  }
}
