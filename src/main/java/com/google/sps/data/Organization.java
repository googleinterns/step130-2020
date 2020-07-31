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

import java.util.ArrayList;
import com.google.sps.data.GivrUser;
import com.google.sps.data.ModeratorInformation;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;

public final class Organization {

  private long id;
  private String name;
  private String email;
  private String address;
  private String city;
  private String state;
  private String zipcode;
  private ArrayList<EmbeddedEntity> hoursOpen;
  private String description;
  private String phoneNum;
  private long creationTimeStampMillis;
  private long lastEditedTimeStampMillis;
  private boolean isApproved;
  private String urlLink;
  private ArrayList<String> resourceCategories;
  private ArrayList<ModeratorInformation> moderators;

  /* An Organization Object takes in an entity and assigns all of its fields based on the entity's
   * properties */

  public Organization(Entity entity) {
    this.id = (long) entity.getKey().getId();
    this.name = (String) entity.getProperty("orgName");
    this.email = (String) entity.getProperty("orgEmail");
    this.address = (String) entity.getProperty("orgStreetAddress");
    this.city = (String) entity.getProperty("orgCity");
    this.state = (String) entity.getProperty("orgState");
    this.zipcode = (String) entity.getProperty("orgZipCode");
    this.hoursOpen = (ArrayList) entity.getProperty("orgHoursOpen");
    this.description = (String) entity.getProperty("orgDescription");
    this.phoneNum = (String) entity.getProperty("orgPhoneNum");
    this.creationTimeStampMillis = (long) entity.getProperty("creationTimeStampMillis");
    this.lastEditedTimeStampMillis = (long) entity.getProperty("lastEditTimeStampMillis");
    this.isApproved = (boolean) entity.getProperty("isApproved");
    this.urlLink = (String) entity.getProperty("orgUrl");
    this.resourceCategories = (ArrayList) entity.getProperty("resourceCategories");

    ArrayList<String> moderatorIds = (ArrayList) entity.getProperty("moderatorList");
    this.moderators = new ArrayList<ModeratorInformation>();
    for(String userId : moderatorIds) {
      GivrUser currUser = GivrUser.getUserById(userId);
      ModeratorInformation moderatorInfo = new ModeratorInformation(userId, currUser.getUserEmail());
      this.moderators.add(moderatorInfo);
    }
  }

  public static Entity getOrgEntityWithId(long orgId) throws IllegalArgumentException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key organizationKey = KeyFactory.createKey("Distributor", orgId);

    Entity organizationEntity = null;
    try {
      organizationEntity = datastore.get(organizationKey);
    } catch (com.google.appengine.api.datastore.EntityNotFoundException err) {
      throw new IllegalArgumentException("Organization entity with orgID " + orgId + " was not found.");
    }

    if (organizationEntity == null) {
      throw new IllegalArgumentException("There is no Organization with ID: " + orgId);
    }

    return organizationEntity;
  }
}
