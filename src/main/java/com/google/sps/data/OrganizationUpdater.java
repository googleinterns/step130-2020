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
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import com.google.appengine.api.datastore.Entity;
import com.google.sps.data.GivrUser;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public final class OrganizationUpdater {

  private Entity entity;

  public OrganizationUpdater(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return this.entity;
  }

  public void updateOrganization(HttpServletRequest request, GivrUser user, boolean forRegistration) throws IllegalArgumentException{
    Set<String> requiresMaintainer = new HashSet<String>();
    Set<String> requiresModerator = new HashSet<String>();
    Map<String, String> properties = new HashMap<String,String>();
    boolean isMaintainer = user.isMaintainer();
    boolean isModerator = user.isModerator();

    requiresMaintainer.add("isApproved");
    requiresModerator.add("moderatorList");

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
      String propertyKey = entry.getValue();
      boolean propertyRequiresMaintainer = requiresMaintainer.contains(propertyKey);
      boolean propertyRequiresModerator = requiresModerator.contains(propertyKey);
      boolean propertyRequiresAuth = requiresMaintainer.contains(propertyKey) || requiresModerator.contains(propertyKey);

      // if updating for registering an organization then do not want to consider fields that require maintainer or moderator permissions
      if(forRegistration && propertyRequiresAuth)) {
          continue;
      }
      // will only get approval param is a maintainer sent the request
      if(propertyRequiresMaintainer && !isMaintainer) {
        continue;
      }

      // will only get moderatorList param if request is sent by a moderator or maintainer
      if(propertyRequiresModerator && !(isModerator || isMaintainer)) {
        continue;
      }

      String formKey = entry.getKey();
      String formValue = "";
      // setting organization description is optional
      if(!propertyKey.equals("orgDescription")) {
        try {
          formValue = getParameterOrThrow(request, formKey);
        } catch(IllegalArgumentException err) {
          throw new IllegalArgumentException();
        }
      } else if (request.getParameter(formKey) != null) {
          formValue = request.getParameter(formKey);
      } else {
          formValue = "";
      }

      setOrganizationProperty(propertyKey, formValue);
    }
  }

  private String getParameterOrThrow(HttpServletRequest request, String formKey) {
    String result = request.getParameter(formKey);
    if(result.isEmpty() || result == null) {
      throw new IllegalArgumentException("Form value cannot be null");
    }
    return result;
  }

  private void setOrganizationProperty(String propertyKey, String formValue) {
    if(propertyKey.equals("moderatorList")) {
      ArrayList<String> newModeratorList = translateEmailsToIds(Arrays.asList(formValue.split("\\s*,\\s*")));
      this.entity.setProperty("moderatorList", newModeratorList);
    } else if(propertyKey.equals("isApproved")) {
      if(formValue.equals("approved")) {
        this.entity.setProperty("isApproved", true);
      } else {
          this.entity.setProperty("isApproved", false);
      }
    } else {
        this.entity.setProperty(propertyKey, formValue);
    }
  }

  private ArrayList<String> translateEmailsToIds(ArrayList<String> emails) {
    ArrayList<String> userIds = new ArrayList<String>();
    for(String email : emails) {
      GivrUser newUser = GivrUser.getUserByEmail(email);
      userIds.add(newUser.getUserId());
    }
    return userIds;
  }
}
