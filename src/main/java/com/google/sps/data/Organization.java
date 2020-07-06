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

public final class Organization {

  private long id;
  private String name;
  private String email;
  private String address;
  private String description;
  private String phoneNum;
  private long openingHour;
  private long closingHour;
  private long creationTimeStamp;
  private long lastEditedTimeStamp;
  private boolean isApproved;
  private String urlLink;
  private ArrayList<String> moderators;

  /* To construct the organization object, we are only worrying about these parameters
   * so it can be created in ListOrganizationServlet & sent back to be displayed in a
   *  popup. GetOrganizationServlet will fill in the rest of these fields w/ setters */

  public Organization(long id, String name, String email, String address, String description, String phoneNum, 
  long openingHour, long closingHour, long creationTimeStamp, long lastEditedTimeStamp, boolean isApproved, String urlLink,
   ArrayList<String> moderators) {
     this.id = id;
     this.name = name;
     this.email = email;
     this.address = address;
     this.description = description;
     this.phoneNum = phoneNum;
     this.openingHour = openingHour;
     this.closingHour = closingHour;
     this.creationTimeStamp = creationTimeStamp;
     this.lastEditedTimeStamp = lastEditedTimeStamp;
     this.isApproved = isApproved;
     this.urlLink = urlLink;
     this.moderators = moderators;
  }
}