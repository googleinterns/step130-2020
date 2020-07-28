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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

public final class EventUpdater {

  private Entity entity;
  private static Logger logger = Logger.getLogger("EventUpdater logger");

  public EventUpdater(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return this.entity;
  }

  public void updateEvent(HttpServletRequest request, GivrUser user, boolean forRegistration, EmbeddedEntity historyUpdate) throws IllegalArgumentException {
    HashMap<String, String> formProperties = new HashMap<String, String>();

    // Format is (Form EntryName, Entity PropertyName)
    formProperties.put("event-name", "eventName");
    formProperties.put("event-owner-org-ids", "eventOwnerOrgId");
    formProperties.put("event-partner", "eventPartnerIdsOrNames");
    formProperties.put("event-details", "eventDetails");
    formProperties.put("event-email", "eventContactEmail");
    formProperties.put("event-phone-num", "eventContactPhone");
    formProperties.put("event-contact-name", "eventContactName");
    formProperties.put("event-street-address", "eventStreetAddress");
    formProperties.put("event-city", "eventCity");
    formProperties.put("event-state", "eventState");
    formProperties.put("event-zip-code", "eventZipcode");


    /* Optional Properties can be left blank in the request form.
     *
     * The following properties are optional:
     * - eventPartnerIdsOrNames
     * - eventDetails
     */
    Set<String> optionalProperties = new HashSet<String>();
    optionalProperties.add("eventPartnerIdsOrNames");
    optionalProperties.add("eventDescription");

    for (Map<String, String> entry: properties.entrySet()) {
      String propertyKey = entry.getValue();
      String formKey = entry.getKey();
      String formValue = "";

      if (optionalProperties.contain(propertyKey)) {
        if (request.getParameter(formKey) != null) {
          formValue = request.getParameter(formKey);
        } else {
          formValue = "";
        }
      } else {
        try {
          formValue = getParameterOrThrow(request, formKey);
        } catch (IllegalArgumentException err) {
          logger.log(Level.SEVERE, "Form value for: " + propertyKey + " cannot be left blank.");
        }
      }

      setEventProperty(propertyKey, formValue);
    }

    setNonFormProperties();

    setEventDateAndHours();
  }

  private String getParameterOrThrow(HttpServletRequest request, String formKey) {
    String result = request.getParameter(formKey);
    if (result == null || result.isEmpty()) {
      throw new IllegalArgumentException();
    }
    return result;
  }

  private void setEventProperty(String propertyKey, String formValue) {

  }

  private void setNonFormProperties(boolean forRegistration) {// determine the parameters
    // TODO: set creation time or last edited based on forRegistration
  }

  private void setEventDateAndHours() { // determine the parameters

    // TODO: Figure out hours + date and put into Event's map.
    // properties.put("event-date-and-hours", "eventDateAndHours");
    // ("event-date", "eventDate")
    // ("event-hours, ??)

    // TODO: extract date and hours from request, then construct embedded entity and set it as eventDateAndHours property
  }

}