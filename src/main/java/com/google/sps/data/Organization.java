
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
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import com.google.appengine.api.datastore.Entity;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public final class Organization {

  private long id;
  private String name;
  private String email;
  private String address;
  private String description;
  private String phoneNum;
  private long creationTimeStampMillis;
  private long lastEditedTimeStampMillis;
  private boolean isApproved;
  private String urlLink;
  private ArrayList<String> moderators;

  /* An Organization Object takes in an entity and assigns all of its fields based on the entity's
   * properties */

  public Organization(Entity entity) {
     this.id = (long) entity.getKey().getId();
     this.name = (String) entity.getProperty("orgName");
     this.email = (String) entity.getProperty("orgEmail");
     this.address = (String) entity.getProperty("orgStreetAddress");
     this.description = (String) entity.getProperty("orgDescription");
     this.phoneNum = (String) entity.getProperty("orgPhoneNum");
     this.creationTimeStampMillis = (long) entity.getProperty("creationTimeStampMillis");
     this.lastEditedTimeStampMillis = (long) entity.getProperty("lastEditTimeStampMillis");
     this.isApproved = (boolean) entity.getProperty("isApproved");
     this.urlLink = (String) entity.getProperty("orgUrl");
     this.moderators = (ArrayList) entity.getProperty("moderatorList");
  }

  public void updateOrganization(HttpServletRequest request, boolean isMaintainer, boolean isModerator) throws IllegalArgumentException{
    Map<String, String> properties = new HashMap<String,String>();

    // Format is Form Name, Entity Property 
    properties.put("org-name", "orgName");
    properties.put("org-email", "orgEmail");
    properties.put("org-street-address", "orgStreetAddress");
    properties.put("org-phone-num", "orgPhoneNum");
    properties.put("org-url", "orgUrl");
    properties.put("org-description","orgDescription");
    properties.put("approval", "isApproved");
    properties.put("moderator-list", "moderatorList");

    for(Map.Entry<String, String> entry : properties.entrySet()) {
      String formKey = entry.getKey();
      String propertyKey = entry.getValue();
      String formValue = "";

      // will only get approval param is a maintainer sent the request
      if(propertyKey.equals("isApproved") && !isMaintainer) {
        continue;
      }

      // will only get moderatorList param if request is sent by a moderator or maintainer
      if(propertyKey.equals("moderatorList") && !isModerator && !isMaintainer) {
        continue;
      }

      // setting organization description is optional
      if(!propertyKey.equals("orgDescription")) {
        try {
          formValue = getParameterOrThrow(request, formKey);
        } catch(IllegalArgumentException err) {
          throw new IllegalArgumentException();
        }
      } else {
        formValue = request.getParameter(formKey);
      }
      setOrganizationProperty(propertyKey, formValue);
    }
  }

  public void commitOrganization(Entity entity) {
    entity.setProperty("orgName", name);
    entity.setProperty("orgEmail", email);
    entity.setProperty("orgStreetAddress", address);
    entity.setProperty("orgDescription", description);
    entity.setProperty("orgPhoneNum", phoneNum);
    entity.setProperty("isApproved", isApproved);
    entity.setProperty("orgUrl", urlLink);
    entity.setProperty("moderatorList", moderators);
  }

  private String getParameterOrThrow(HttpServletRequest request, String formKey) {
    String result = request.getParameter(formKey);
    if(result.isEmpty()) {
        throw new IllegalArgumentException("Form value cannot be null");
    }
    return result;
  }

  private void setOrganizationProperty(String propertyKey, String formValue) {
    if(propertyKey.equals("moderatorList")) {
      moderators = new ArrayList<String>(Arrays.asList(formValue.split("\\s*,\\s*")));
    } else if(propertyKey.equals("isApproved")) {
      if(formValue.equals("approved")) {
        isApproved = true;
      } else if(formValue.equals("notApproved")) {
          isApproved = false;
        }
    } else if(propertyKey.equals("orgName")) {
      name = formValue;
    } else if(propertyKey.equals("orgEmail")) {
      email = formValue;
    } else if(propertyKey.equals("orgStreetAddress")) {
      address = formValue;
    } else if(propertyKey.equals("orgDescription")) {
      description = formValue;
    } else if(propertyKey.equals("orgPhoneNum")) {
      phoneNum = formValue;
    } else if(propertyKey.equals("orgUrl")) {
      urlLink = formValue;
    }
  }
}
