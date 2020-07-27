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
import com.google.sps.data.Contact;

public class Event {

  // Represents ID of Event; Type is long because Datastore's Key's getId() return type is long.
  private long id;
  // Represents the title of the Event.
  private String title;
  // Holds IDs (type long) of Organizations that own the Event.
  private ArrayList<Long> ownerOrgIds;
  // Holds EmbeddedEntity that represents an Organization's ID OR name. A partnering organization could have missing ID.
  private ArrayList<EmbeddedEntity> partnerIdsOrNames;
  // Represents description of the Event. e.g. "In front of church ABC"
  private String description;
  // Holds dates of Event and their corresponding time(s).
  private Map<Date, EmbeddedEntity> dateAndHours;
  // Holds contact information for this Event.
  private Contact contactInformation;
  // Represents when Event was created. Type is long because java.time.Instant returns a long type.
  private long creationTimeStampMillis;
  // Represents when Event was last updated.
  private long lastEditedTimeStampMillis;
  // Represents location information for the Event.
  private String address;
  private String city;
  private String state;
  private String zipcode;

  public Event() {
    // TODO: Set necessary parameters for Constructor.
  }

}
