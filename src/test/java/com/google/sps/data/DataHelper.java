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

package com.google.sps.data;

import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;


 /*
  * DataHelper prepares the Datastore data (Distributor and User) and sets up the LocalServiceTestHelper.
  */
public class DataHelper {

  private LocalServiceTestHelper helper;
  private ArrayList<Entity> listOfUserEntities = new ArrayList<>();
  private ArrayList<Entity> listOfDistributorEntities = new ArrayList<>();
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  LocalUserServiceTestConfig userServiceConfig;

  private void addUsersToDatastore() {

    /*                    User Table
       TYPES:
         String      boolean               String
      +----------+--------------+--------------------------+
      |  userId  | isMaintainer |        userEmail         |
      +----------+--------------+--------------------------+
      |   User0  |    true      |  baikj+test0@google.com  |
      +----------+--------------+--------------------------+
      |   User1  |    false     |  baikj+test1@google.com  |
      +----------+--------------+--------------------------+
      |   User2  |    false     |  baikj+test2@google.com  |
      +----------+--------------+--------------------------+
      |   User3  |    false     |  baikj+test3@google.com  |
      +----------+--------------+--------------------------+
      |   User4  |    false     |  baikj+test4@google.com  |
      +----------+--------------+--------------------------+
      |   User5  |    false     |  baikj+test5@google.com  |
      +----------+--------------+--------------------------+
      |   User6  |    false     |  baikj+test6@google.com  |
      +----------+--------------+--------------------------+
      |   User7  |    false     |  baikj+test7@google.com  |
      +----------+--------------+--------------------------+
      |   User8  |    false     |  baikj+test8@google.com  |
      +----------+--------------+--------------------------+
      |   User9  |    false     |  baikj+test9@google.com  |
      +----------+--------------+--------------------------+

     */

    // Represents the only Maintainer in our User table.
    Entity user0 = new Entity("User");
    user0.setProperty("userId", "User0");
    user0.setProperty("isMaintainer", true);
    user0.setProperty("userEmail", "baikj+test0@google.com");
    datastore.put(user0);
    listOfUserEntities.add(user0);

    // Generates nine users who are not Maintainers. Will be set as Moderators when adding Distributors.
    for (int i = 1; i < 10; i++) {
      Entity user = new Entity("User");
      user.setProperty("userId", "User" + Integer.toString(i));
      user.setProperty("isMaintainer", false);
      user.setProperty("userEmail", "baikj+test" + Integer.toString(i) + "@google.com");
      datastore.put(user);
      listOfUserEntities.add(user);
    }

  }

  private void addDistributorsToDatastore() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    /* Distributor Table

      TYPES:
                          ArrayList<String>      ArrayList<String>
       long    String      String: userID        String: userEmails
     +------+-----------+-------------------+---------------------------+
     |  ID  |  orgName  |   moderatorList   |     invitedModerators     |
     +------+-----------+-------------------+---------------------------+
     |  0   |   Org0    |   User0, User1    |   baikj+test9@google.com  |
     +------+-----------+-------------------+---------------------------+
     |  1   |   Org1    |   User2, User3    |   baikj+test8@google.com  |
     +------+-----------+-------------------+---------------------------+
     |  2   |   Org2    |       User4       |   baikj+test7@google.com  |
     |      |           |                   |   baikj+test6@google.com  |
     +------+-----------+-------------------+---------------------------+

     */

    String org0Moderators[] = new String[] {"User0", "User1"};
    String org0InvitedMods[] = new String[] {"baikj+test9@google.com"};

    Entity distributor0 = new Entity("Distributor");
    distributor0.setProperty("orgName", "Org0");
    distributor0.setProperty("moderatorList", Arrays.asList(org0Moderators));
    distributor0.setProperty("invitedModerators", Arrays.asList(org0InvitedMods));
    datastore.put(distributor0);
    listOfDistributorEntities.add(distributor0);

    String org1Moderators[] = new String[] {"User2", "User3"};
    String org1InvitedMods[] = new String[] {"baikj+test8@google.com"};

    Entity distributor1 = new Entity("Distributor");
    distributor1.setProperty("orgName", "Org1");
    distributor1.setProperty("moderatorList", Arrays.asList(org1Moderators));
    distributor1.setProperty("invitedModerators", Arrays.asList(org1InvitedMods));
    datastore.put(distributor1);
    listOfDistributorEntities.add(distributor1);

    String org2Moderators[] = new String[] {"User4"};
    String org2InvitedMods[] = new String[] {"baikj+test7@google.com", "baikj+test6@google.com"};

    Entity distributor2 = new Entity("Distributor");
    distributor2.setProperty("orgName", "Org2");
    distributor2.setProperty("moderatorList", Arrays.asList(org2Moderators));
    distributor2.setProperty("invitedModerators", Arrays.asList(org2InvitedMods));
    datastore.put(distributor2);
    listOfDistributorEntities.add(distributor2);

  }

  private DatastoreService getDatastore() {
    return datastore;
  }

  // If user is a Maintainer, userId is not expected to have a value.
  private void setUserServiceConfig(boolean userIsMaintainer, String userId) {
    if (userIsMaintainer) {
      setUserAsMaintainer();
    } else {
      setUserAs(userId);
    }
  }

  // Sets up local UserService test config with Maintainer info from User table.
  private void setUserAsMaintainer() {
    userServiceConfig = new LocalUserServiceTestConfig();
    userServiceConfig.setOAuthEmail("baikj+test0@google.com");
    userServiceConfig.setOAuthUserId("User0");
    userServiceConfig.setOAuthAuthDomain("google.com");
  }

  // Sets up local UserService test config with Moderator (User 0-4, 6-9) info from User table.
  private void setUserAs(String userId) {
    userServiceConfig = new LocalUserServiceTestConfig();
    userServiceConfig.setOAuthEmail("baikj+test" + userId.charAt(4) + "@google.com");
    userServiceConfig.setOAuthUserId(userId);
    userServiceConfig.setOAuthAuthDomain("google.com");
  }

  private LocalUserServiceTestConfig getUserServiceConfig() {
    return userServiceConfig;
  }

  public ArrayList<Entity> getListOfUserEntities() {
    return listOfUserEntities;
  }

  public ArrayList<Entity> getListOfDistributorEntities() {
    return listOfDistributorEntities;
  }

  // Sets up Datastore and UserService local test configs and sets up environment variable based on what kind of User is being tested.
  public LocalServiceTestHelper setUpAndReturnLocalServiceTestHelper(boolean userIsMaintainer, String userId) {
    LocalDatastoreServiceTestConfig datastoreConfig = new LocalDatastoreServiceTestConfig();

    addUsersToDatastore();
    addDistributorsToDatastore();

    setUserServiceConfig(userIsMaintainer, userId);

    LocalUserServiceTestConfig userServiceConfig = getUserServiceConfig();

    helper = new LocalServiceTestHelper(datastoreConfig, userServiceConfig);
    helper.setEnvIsLoggedIn(true);
    helper.setEnvAuthDomain("google.com");
    Map<String,Object> envAttributeMap = new HashMap<String,Object>();

    if (userIsMaintainer) {
      helper.setEnvEmail("baikj+test0@google.com");
      envAttributeMap.put("com.google.appengine.api.users.UserService.user_id_key", "User0");
    } else {
      helper.setEnvEmail("baikj+test" + userId.charAt(4) + "@google.com");
      envAttributeMap.put("com.google.appengine.api.users.UserService.user_id_key", userId);
    }

    helper.setEnvAttributes(envAttributeMap);
    helper.setUp();

    return helper;
  }
}
