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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Key;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class EventUpdater {

  private Entity entity;
  private static Logger logger = Logger.getLogger("EventUpdater logger");

  public EventUpdater(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return this.entity;
  }

  // Called when creating and updating Event from AddEventServlet and EditEventServlet.
  public void updateEvent(HttpServletRequest request, GivrUser user, boolean forRegistration, EmbeddedEntity historyUpdate) throws IllegalArgumentException {    
    // Must check if requesting User is a Moderator of the Event's Organization.
    long ownerOrgId = 0;
    try {
      ownerOrgId = Long.parseLong(getParameterOrThrow(request, "event-primary-organization-id"));
    } catch (IllegalArgumentException err) {
      logger.log(Level.SEVERE, "The primary organization ID is not valid.");
    }

    if (!doesUserHasCredentialsToUpdateEvent(user, ownerOrgId)) {
      throw new IllegalArgumentException("Requesting user does not have the right credentials to create or update this Event.");
    }

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

    for (Map.Entry<String, String> entry: formProperties.entrySet()) {
      String propertyKey = entry.getValue();
      String formKey = entry.getKey();
      String formValue = "";

      if (formKey.equals("event-primary-organization-id")) {
        continue;
      }

      if (optionalProperties.contains(propertyKey)) {
        formValue = request.getParameter(formKey) == null ? "" : request.getParameter(formKey);
      } else {
        try {
          formValue = getParameterOrThrow(request, formKey);
        } catch (IllegalArgumentException err) {
          logger.log(Level.SEVERE, "Form value for: " + propertyKey + " cannot be left blank.");
        }
      }

      setEventProperty(propertyKey, formValue);
    }

    setNonFormProperties(forRegistration, historyUpdate);

    setEventDateAndHours(request);

    setEventOwnerOrgNameBasedOnId(ownerOrgId);
  }

  private Entity getOrgEntityWithId(long orgId) throws IllegalArgumentException {
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

  private boolean doesUserHasCredentialsToUpdateEvent(GivrUser user, long orgId) {
    Entity entity = getOrgEntityWithId(orgId);
    ArrayList<String> moderatorList = (ArrayList) entity.getProperty("moderatorList");

    String userId = user.getUserId();
    return moderatorList.contains(userId);
  }

  private void setEventOwnerOrgNameBasedOnId(long ownerOrgId) {
    Entity entity = getOrgEntityWithId(ownerOrgId);
    
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

  private ArrayList<String> getParameterValuesOrThrow(HttpServletRequest request, String formKey){
    ArrayList<String> results = new ArrayList<String>(Arrays.asList(request.getParameterValues(formKey)));
    if (results.isEmpty() || results == null) {
      throw new IllegalArgumentException("Form value cannot be null");
    }

    // Checks if there is a value that is empty which means a blank time range was submitted
    for(int i = 0; i < results.size(); i++) {
      if(results.get(i).equals("")) {
        throw new IllegalArgumentException("Form value cannot be null");
      }
    }
    return results;
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
      this.entity.setProperty("creationTimeStampMillis", milliSecondsSinceEpoch);
    } else {
      // If not registering event, changeHistory property should exist and should be modified.
      changeHistory = (ArrayList) this.entity.getProperty("changeHistory");
    }

    this.entity.setProperty("lastEditTimeStampMillis", milliSecondsSinceEpoch);
    changeHistory.add(historyUpdate);
    this.entity.setProperty("changeHistory", changeHistory);
  }

  private void setEventDateAndHours(HttpServletRequest request) {
    // Example of how event-date would be passed in: "2020-07-10"
    String eventDate = request.getParameter("event-date");

    Date date = null;
    try {
      date = new SimpleDateFormat("yyyy-MM-dd").parse(eventDate);
    } catch (java.text.ParseException err) {
      logger.log(Level.SEVERE, "Date information is in the wrong format.");
    }

    HashMap<Date, EmbeddedEntity> dateAndHours = new HashMap<Date, EmbeddedEntity>();

    EmbeddedEntity hoursEmbeddedEntity = new EmbeddedEntity();

    ArrayList<String> eventFromTime = getParameterValuesOrThrow(request, "Event hours-from-times");
    ArrayList<String> eventToTime = getParameterValuesOrThrow(request, "Event hours-to-times");

    hoursEmbeddedEntity.setProperty("event-from-time", eventFromTime);
    hoursEmbeddedEntity.setProperty("event-to-time", eventToTime);

    dateAndHours.put(date, hoursEmbeddedEntity);

    this.entity.setProperty("eventDateAndHours", dateAndHours);
  }

}
