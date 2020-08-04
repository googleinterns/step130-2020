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
import com.google.sps.data.RequestHandler;
import com.google.sps.data.ParserHelper;
import com.google.sps.data.Organization;

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
      ownerOrgId = Long.parseLong(RequestHandler.getParameterOrThrow(request, "event-primary-organization-id"));
    } catch (IllegalArgumentException err) {
      logger.log(Level.SEVERE, "The primary organization ID is not valid.");
      throw new IllegalArgumentException();
    }
    if (!user.isModeratorOfOrgWithId(ownerOrgId)) {
      throw new IllegalArgumentException("Requesting user does not have the right credentials to create or update this Event.");
    }

    HashMap<String, String> formProperties = new HashMap<String, String>();

    // Format is (Form EntryName, Entity PropertyName)
    formProperties.put("event-primary-organization-id", "ownerOrgId");
    formProperties.put("event-name", "name");
    formProperties.put("event-partner", "partnerOrgNames");
    formProperties.put("event-details", "description");
    formProperties.put("event-contact-email", "email");
    formProperties.put("event-contact-phone-num", "phone");
    formProperties.put("event-contact-name", "contactName");
    formProperties.put("event-street-address", "streetAddress");
    formProperties.put("event-city", "city");
    formProperties.put("event-state", "state");
    formProperties.put("event-zip-code", "zipcode");

    /* Optional Properties can be left blank in the request form.
     *
     * The following properties are optional:
     * - partnerOrgNames
     * - eventDetails
     */
    Set<String> optionalProperties = new HashSet<String>();
    optionalProperties.add("partnerOrgNames");
    optionalProperties.add("description");

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
          formValue = RequestHandler.getParameterOrThrow(request, formKey);
        } catch (IllegalArgumentException err) {
          logger.log(Level.SEVERE, "Form value for: " + propertyKey + " cannot be left blank.");
          throw new IllegalArgumentException();
        }
      }

      setEventProperty(propertyKey, formValue);
    }

    setNonFormProperties(forRegistration, historyUpdate);

    setEventDateAndHours(request);

    setEventOwnerOrgIdAndName(ownerOrgId);
  }

  private void setEventOwnerOrgIdAndName(long ownerOrgId) {
    Entity entity = Organization.getOrgEntityWithId(ownerOrgId);
    
    String ownerOrgName = (String) entity.getProperty("name");
    this.entity.setProperty("ownerOrgName", ownerOrgName);
    this.entity.setProperty("ownerOrgId", ownerOrgId);
  }

  // Sets form values based on property key.
  private void setEventProperty(String propertyKey, String formValue) {
    if (propertyKey.equals("partnerOrgNames")) {
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
      throw new IllegalArgumentException();
    }

    ArrayList<EmbeddedEntity> dateAndHours = new ArrayList<EmbeddedEntity>();

    ArrayList<String> eventFromTime = RequestHandler.getParameterValuesOrThrow(request, "Event Hours-from-times");
    ArrayList<String> eventToTime = RequestHandler.getParameterValuesOrThrow(request, "Event Hours-to-times");
    ArrayList<EmbeddedEntity> fromToPairs = ParserHelper.createHoursFromAndHoursToPairs(eventFromTime, eventToTime);

    EmbeddedEntity dateAndHoursEmbeddedEntity = new EmbeddedEntity();
    dateAndHoursEmbeddedEntity.setProperty("date", date);
    dateAndHoursEmbeddedEntity.setProperty("fromToPairs", fromToPairs);

    dateAndHours.add(dateAndHoursEmbeddedEntity);
    this.entity.setProperty("dateAndHours", dateAndHours);
  }
}
