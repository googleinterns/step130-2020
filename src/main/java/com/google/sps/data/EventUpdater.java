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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.sps.data.GivrUser;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.PreparedQuery;

public final class EventUpdater {

  private Entity entity;
  private static Logger logger = Logger.getLogger("EventUpdater logger");

  public EventUpdater(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return this.entity;
  }

  // TODO: When editing event (after event has been registered), we must check if the user sending the request has the right credentials - add GivrUser.isModeratorOfOrgWithId(long organizationId)
  public void updateEvent(HttpServletRequest request, GivrUser user, boolean forRegistration, EmbeddedEntity historyUpdate) throws IllegalArgumentException {
    HashMap<String, String> formProperties = new HashMap<String, String>();

    // Format is (Form EntryName, Entity PropertyName)
    formProperties.put("event-primary-organization-id", "eventOwnerOrgId");
    formProperties.put("event-name", "eventName");
    formProperties.put("event-partner", "eventPartnerNames");
    formProperties.put("event-details", "eventDetails");
    formProperties.put("event-contact-email", "eventContactEmail");
    formProperties.put("event-contact-phone-num", "eventContactPhone");
    formProperties.put("event-contact-name", "eventContactName");
    formProperties.put("event-street-address", "eventStreetAddress");
    formProperties.put("event-city", "eventCity");
    formProperties.put("event-state", "eventState");
    formProperties.put("event-zip-code", "eventZipcode");

    /* Optional Properties can be left blank in the request form.
     *
     * The following properties are optional:
     * - eventPartnerNames
     * - eventDetails
     */
    Set<String> optionalProperties = new HashSet<String>();
    optionalProperties.add("eventPartnerNames");
    optionalProperties.add("eventDetails");

    long ownerOrgId;

    for (Map.Entry<String, String> entry: formProperties.entrySet()) {
      String propertyKey = entry.getValue();
      String formKey = entry.getKey();
      String formValue = "";

      if (optionalProperties.contains(propertyKey)) {
        formValue = request.getParameter(formKey) == null ? "" : request.getParameter(formKey);
      } else {
        try {
          formValue = getParameterOrThrow(request, formKey);
        } catch (IllegalArgumentException err) {
          logger.log(Level.SEVERE, "Form value for: " + propertyKey + " cannot be left blank.");
        }

        if (formKey.equals("event-primary-organization-id")) {
          ownerOrgId = Long.parseLong(formValue);
        }
      }

      setEventProperty(propertyKey, formValue);
    }

    setNonFormProperties(forRegistration, historyUpdate);

    setEventDateAndHours(request);

    setEventOwnerOrgNameBasedOnId(ownerOrgId);
  }

  private void setEventOwnerOrgNameBasedOnId(long ownerOrgId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Filter queryFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, ownerOrgId);
    Query query = new Query("Distributor").setFilter(queryFilter);
    PreparedQuery preparedQuery = datastore.prepare(query);

    Entity entity = null;
    try {
      entity = preparedQuery.asSingleEntity();
    } catch(PreparedQuery.TooManyResultsException exception) {
      logger.log(Level.SEVERE, "Multiple Distributor entities found with ID: " + Long.toString(ownerOrgId) + ".");
    }

    String ownerOrgName = (String) entity.getProperty("orgName");
    this.entity.setProperty("eventOwnerOrgName", ownerOrgName);
  }

  private String getParameterOrThrow(HttpServletRequest request, String formKey) {
    String result = request.getParameter(formKey);
    if (result == null || result.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return result;
  }

  // Sets form values based on property key.
  private void setEventProperty(String propertyKey, String formValue) {
    if (propertyKey.equals("eventPartnerNames")) {
      // Stores partnering organizations's names; partnering organizations do not have the ability to edit Event, so there is no need to store org IDs.
      ArrayList<String> parsedNames = new ArrayList<String>(Arrays.asList(formValue.split("\\s*,\\s*")));

      this.entity.setProperty(propertyKey, parsedNames);
      return;
    }

    this.entity.setProperty(propertyKey, formValue);
  }

  // Sets values not passed in through the request form such as creation time of Event and last edited time.
  private void setNonFormProperties(boolean forRegistration, EmbeddedEntity historyUpdate) {
    long milliSecondsSinceEpoch = (long) historyUpdate.getProperty("changeTimeStampMillis");
    ArrayList<EmbeddedEntity> changeHistory = new ArrayList<EmbeddedEntity>();

    if (forRegistration) {
      this.entity.setProperty("eventCreationTimeStampMillis", milliSecondsSinceEpoch);
    } else {
      // If not registering event, changeHistory property should exist and should be modified.
      changeHistory = (ArrayList) this.entity.getProperty("changeHistory");
    }

    this.entity.setProperty("eventLastEditTimeStampMillis", milliSecondsSinceEpoch);
    changeHistory.add(historyUpdate);
    this.entity.setProperty("changeHistory", changeHistory);
  }

  private void setEventDateAndHours(HttpServletRequest request) {

    String eventDate = request.getParameter("event-date");

    // TODO: 
    // 1) extract date and hours from request, then construct embedded entity and set it as eventDateAndHours property
    // 2) decide on format of date MM/DD/YYYY (in front end as well)
    // 3) discuss how hours is sent (We don't need "Mon"-"Fri" that Organizations have.)
  }

}
