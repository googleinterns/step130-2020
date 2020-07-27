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
import java.util.Date;
import java.util.ArrayList;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;

public class Event {

  private long id;
  private String title;
  private ArrayList<String> ownerOrgIds;
  private ArrayList<EmbeddedEntity> partnerIdsOrNames;
  private String description;
  private Map<Date, EmbeddedEntity> dateAndHours;
  private String contactEmail;
  private String contactPhone;
  private String address;
  private String city;
  private String state;
  private String zipcode;
  private long creationTimeStampMillis;
  private long lastEditedTimeStampMillis;

  public Event(Entity entity) {
    this.id = (long) entity.getKey().getId();
    this.title = (String) entity.getProperty("eventTitle");
    this.ownerOrgIds = (ArrayList) entity.getProperty("eventOwnerOrgIds");
    this.partnerIdsOrNames = (ArrayList) entity.getProperty("eventPartnerOrgIdsOrNames");
    this.description = (String) entity.getProperty("eventDescription");
    this.contactEmail = (String) entity.getProperty("eventEmail");
    this.address = (String) entity.getProperty("eventStreetAddress");
    this.city = (String) entity.getProperty("eventCity");
    this.state = (String) entity.getProperty("eventState");
    this.zipcode = (String) entity.getProperty("eventZipCode");
    this.dateAndHours = (Map) entity.getProperty("dateAndHours");
    this.contactPhone = (String) entity.getProperty("eventPhoneNum");
    this.creationTimeStampMillis = (long) entity.getProperty("creationTimeStampMillis");
    this.lastEditedTimeStampMillis = (long) entity.getProperty("lastEditTimeStampMillis");
  }
}
