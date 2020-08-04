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

import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;

public class Event {

  // Represents ID of Event; Type is long because Datastore's Key's getId() return type is long.
  private long id;
  // Represents the name of the Event.
  private String name;
  // Holds ID (type String) of the Organization that owns the Event.
  private long ownerOrgId;
  // Holds name of the Organization that owns the Event. Is set in EventUpdater's setEventOwnerOrgName().
  private String ownerOrgName;
  /* Holds ArrayList of Strings that represents an Organization's name. 
   * No need to store IDs of partnering organization, as partners will not have the privilege of editing Events.
   */
  private ArrayList<String> partnerNames;
  // Represents details of the Event. e.g. "In front of church ABC"
  private String details;
  // Holds Date of Event and their corresponding time(s). EmbeddedEntity holds a Date and an ArrayList of EmbeddedEntities that represents the time range(s) for that specific Date. This was chosen over a Map because Datastore does not support Maps and will be mainly used in servlets + modifying data in Datastore.
  private ArrayList<EmbeddedEntity> dateAndHours;

  /* Holds contact information for this Event. */

  // Example: help@givr.com
  private String contactEmail;
  // Example: 3923421930 (Assume +1 is prepended)
  private String contactPhone;
  // Example: Bob Jones
  private String contactName;


  /* Holds location information for the Event. All addresses used in this application are in the United States. */

  /* Street Address includes street name and house number/apartment/suite/room number (if any)
   * Example: 1600 Amphitheatre Parkway
   */
  private String streetAddress;
  // Example: Mountain View
  private String city;
  // Example: CA/California
  private String state;
  /* Represents five digit zipcode.
   * Example: 94043
   */
  private String zipcode;

  /* An Event object takes in an entity and assigns all of its fields based on the entity's properties. */
  
  public Event(Entity entity) {
    this.id = (long) entity.getKey().getId();
    this.name = (String) entity.getProperty("name");
    this.ownerOrgId = (long) entity.getProperty("ownerOrgId");
    this.ownerOrgName = (String) entity.getProperty("ownerOrgName");
    this.partnerNames = (ArrayList) entity.getProperty("partnerOrgNames");
    this.details = (String) entity.getProperty("description");
    this.dateAndHours = (ArrayList) entity.getProperty("dateAndHours");
    this.contactEmail = (String) entity.getProperty("email");
    this.contactPhone = (String) entity.getProperty("phone");
    this.contactName = (String) entity.getProperty("contactName");
    this.streetAddress = (String) entity.getProperty("streetAddress");
    this.city = (String) entity.getProperty("city");
    this.state = (String) entity.getProperty("state");
    this.zipcode = (String) entity.getProperty("zipcode");
  }
}
