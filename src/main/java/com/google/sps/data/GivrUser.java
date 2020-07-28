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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Object;
import java.util.logging.Logger;
import java.util.logging.Level;


public class GivrUser {

  private static Logger logger = Logger.getLogger("GivrUser Logger");
  private String id;
  private boolean isMaintainer;
  private boolean isLoggedIn;
  private String url;
  private String email;
  // In method setModeratingOrgs(), ArrayList will be set with all Organizations that this GivrUser moderates.
  // Should be accessed with getModeratingOrgs() to retrieve the most up to date moderatingOrgs
  private ArrayList<Entity> moderatingOrgs = new ArrayList<Entity>();

  public GivrUser(String id, boolean isMaintainer, boolean isLoggedIn, String url, String email) {
    this.id = id;
    this.isMaintainer = isMaintainer;
    this.isLoggedIn = isLoggedIn;
    this.url = url;
    this.email = email;
  }

  public String getUserId() {
    return this.id;
  }

  public boolean isMaintainer() {
    return this.isMaintainer;
  }

  public String getUserEmail() {
    return this.email;
  }

  public boolean isLoggedIn() {
    return this.isLoggedIn;
  }

  public boolean isModeratorOfOrganization(long organizationId) {
    for(Entity entity : getModeratingOrgs()) {
      long entityId = (long) entity.getKey().getId();
      if(organizationId == entityId) {
        return true;
      }
    }
    return false;
  }

  public ArrayList<Entity> getModeratingOrgs() {
    setModeratingOrgs(); // Makes sure that getModeratingOrgs is returning updated information.
    return this.moderatingOrgs;
  }

  public boolean isModeratorOfAnyOrg() {
    return getModeratingOrgs().size() > 0;
  }

  // Gets User with propertyName, propertyValue exists within Datastore.
  public static Entity getUserFromDatastoreWithProperty(String propertyName, String propertyValue) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Filter queryFilter = new FilterPredicate(propertyName, FilterOperator.EQUAL, propertyValue);
    Query query = new Query("User").setFilter(queryFilter);
    PreparedQuery preparedQuery = datastore.prepare(query);

    Entity entity = null;
    try {
      entity = preparedQuery.asSingleEntity();
    } catch(PreparedQuery.TooManyResultsException exception) {
      logger.log(Level.SEVERE, "Multiple User entities found with property name: " + propertyName + " and property value: " + propertyValue + ".");
    }
    return entity; // Entity can be null.
  }

  // Updates a User entity in Datastore, identifying with the first two parameters with values from second two parameters.
  public static void updateUserInDatastore(String identifyingProperty, String identifyingValue, Map<String, Object> updatePropertyNamesAndValues) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = getUserFromDatastoreWithProperty(identifyingProperty, identifyingValue);

    if (entity == null) {
      throw new Error("User with " + identifyingProperty + ": " + identifyingValue + " was not found.");
    }

    for (Map.Entry<String, Object> entry: updatePropertyNamesAndValues.entrySet()) {
      entity.setProperty(entry.getKey(), entry.getValue());
    }
    datastore.put(entity);
  }

  // Updates Organization entities in Datastore by removing user's email from invitedModerators list and adding user's ID in moderatorlist.
  public void updateModeratingOrgs() {
    for (int i = 0; i < this.moderatingOrgs.size(); i++) {
      OrganizationUpdater organizationUpdater = new OrganizationUpdater(this.moderatingOrgs.get(i));
      organizationUpdater.updateInvitedModerator(this);
    }
  }

  // Properly sets user's moderatingOrgs based on results from querying to Datastore for Organization entity's moderatorList and invitedModerators list. Does not update to the Datastore Organization table.
  public void setModeratingOrgs() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    this.moderatingOrgs = new ArrayList<Entity>();

    Query query = new Query("Distributor").addSort("creationTimeStampMillis", SortDirection.DESCENDING);

    ArrayList<Filter> individualFilterCollection = new ArrayList<Filter>();

    individualFilterCollection.add(new FilterPredicate("moderatorList", FilterOperator.EQUAL, this.id));
    individualFilterCollection.add(new FilterPredicate("invitedModerators", FilterOperator.EQUAL, this.email));

    CompositeFilter compositeORFilter = new CompositeFilter(CompositeFilterOperator.OR, individualFilterCollection);

    PreparedQuery preparedQuery = datastore.prepare(query.setFilter(compositeORFilter));

    for (Entity entity: preparedQuery.asIterable()) {
      this.moderatingOrgs.add(entity);
    }
  }

  private static GivrUser getUserByIdOrEmail(String userId, String userEmail) {
    Entity entityRetrievedWithId = null;
    Entity entityRetrievedWithEmail = null;

    if (userId != null) {
      entityRetrievedWithId = getUserFromDatastoreWithProperty("userId", userId);
    }
    if (userEmail != null) {
      entityRetrievedWithEmail = getUserFromDatastoreWithProperty("userEmail", userEmail);
    }

    boolean isMaintainer = false;
    boolean isLoggedIn = true;

    if (entityRetrievedWithId != null) {
      isMaintainer = (boolean) entityRetrievedWithId.getProperty("isMaintainer");
      userEmail = (String) entityRetrievedWithId.getProperty("userEmail");
    } else if (entityRetrievedWithEmail != null) {
      isMaintainer = (boolean) entityRetrievedWithEmail.getProperty("isMaintainer");
      userId = (String) entityRetrievedWithEmail.getProperty("userId");
    }
    
    GivrUser user = new GivrUser(userId, isMaintainer, isLoggedIn, "" /* URL is not needed when User is logged in. */, userEmail);
    return user;
  }

  public static GivrUser getUserById(String userId) {
    return getUserByIdOrEmail(userId, null);
  }

  public static GivrUser getUserByEmail(String email) {
    // TODO: Support OAuth.
    return getUserByIdOrEmail(null, email);
  }

  // The email returned in the GivrUser object is the value in the Datastore.
  public static GivrUser getCurrentLoggedInUser() {
    UserService userService = UserServiceFactory.getUserService();
    boolean isUserLoggedIn = userService.isUserLoggedIn();
    String url = "";
    
    if (isUserLoggedIn) {
      return getUserByIdOrEmail(userService.getCurrentUser().getUserId(), userService.getCurrentUser().getEmail());
    }
    url = userService.createLoginURL("/");

    return new GivrUser("" /* userId */, false /* isMaintainer */, false /* isLoggedIn */, url /* loginURL */, "" /* userEmail */);
  }

  public boolean equals(Object userObject) {
    GivrUser user = (GivrUser) userObject;
    
    // Two GivrUser objects will be equal to each other when they have the same userEmail and userId.
    return this.getUserEmail().equals(user.getUserEmail()) && this.getUserId().equals(user.getUserId());
  }
}
