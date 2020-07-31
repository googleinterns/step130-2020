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
import java.text.SimpleDateFormat;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.sps.data.GivrUser;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import com.google.sps.data.RequestHandler;
import com.google.sps.data.ParserHelper;

public final class OrganizationUpdater {

  enum DayOfWeek {
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday
  }

  private Entity entity;

  public OrganizationUpdater(Entity entity) {
    this.entity = entity;
  }

  public Entity getEntity() {
    return this.entity;
  }

  public void updateOrganization(HttpServletRequest request, GivrUser user, boolean forRegistration, EmbeddedEntity historyUpdate) throws IllegalArgumentException{
    Set<String> requiresMaintainer = new HashSet<String>();
    Set<String> requiresModerator = new HashSet<String>();
    Map<String, String> properties = new HashMap<String,String>();
    boolean isMaintainer = user.isMaintainer();
    boolean isModerator = user.isModeratorOfAnyOrg();

    requiresMaintainer.add("isApproved");
    requiresModerator.add("moderatorList");

    // Format is Form Name, Entity Property 
    properties.put("org-name", "name");
    properties.put("org-email", "email");
    properties.put("org-street-address", "streetAddress");
    properties.put("org-city", "city");
    properties.put("org-state", "state");
    properties.put("org-zip-code", "zipcode");
    properties.put("org-phone-num", "phone");
    properties.put("org-url", "url");
    properties.put("org-description","description");
    properties.put("approval", "isApproved");
    properties.put("moderator-list", "moderatorList");
    properties.put("org-resource-categories", "resourceCategories");

    // Updates most entity properties from form
    for(Map.Entry<String, String> entry : properties.entrySet()) {
      String propertyKey = entry.getValue();
      boolean propertyRequiresMaintainer = requiresMaintainer.contains(propertyKey);
      boolean propertyRequiresModerator = requiresModerator.contains(propertyKey);
      boolean propertyRequiresAuth = requiresMaintainer.contains(propertyKey) || requiresModerator.contains(propertyKey);

      // if updating for registering an organization then do not want to consider fields that require maintainer or moderator permissions
      if(forRegistration && propertyRequiresAuth) {
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
      if(!propertyKey.equals("description")) {
        try {
          formValue = RequestHandler.getParameterOrThrow(request, formKey);
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

    // updates open hours property separate since it is more complex
    updateOpenHoursProperty(request);

    // Updates non form properties such as change history, lastEditTimeStamp, etc
    updateNonFormProperties(user, forRegistration, historyUpdate);

  }

  private void setOrganizationProperty(String propertyKey, String formValue) {
    if(propertyKey.equals("moderatorList")) {
      // Separates emails entered into moderatorList form value into IDs for moderatorList and Emails for invitedModerators.

      ArrayList<String> parsedEmailList = new ArrayList<String> (Arrays.asList(formValue.split("\\s*,\\s*")));
       // Based on the regex parsed list of emails, following methods will add appropriate userIds or userEmails to moderatorIds or invitedModeratorEmails, respectively.
      ArrayList<String> newModeratorList = findAndRetrieveModeratorList(parsedEmailList);
      ArrayList<String> newInvitedModerators = findAndRetrieveInvitedModerators(parsedEmailList);

      this.entity.setProperty("moderatorList", newModeratorList);
      this.entity.setProperty("invitedModerators", newInvitedModerators);
    } else if(propertyKey.equals("isApproved")) {
      if(formValue.equals("approved")) {
        this.entity.setProperty("isApproved", true);
      } else {
        this.entity.setProperty("isApproved", false);
      }
    } else if (propertyKey.equals("resourceCategories")) {
      ArrayList<String> resourceList = new ArrayList<String>(Arrays.asList(formValue.split("\\s*,\\s*")));
      this.entity.setProperty("resourceCategories", resourceList);
    } else {
        this.entity.setProperty(propertyKey, formValue);
    }
  }

  private ArrayList<String> findAndRetrieveInvitedModerators(ArrayList<String> emails) {
    ArrayList<String> invitedModeratorEmails = new ArrayList<String>();
    for(String email : emails) {
      GivrUser newUser = GivrUser.getUserByEmail(email);
      String userId = newUser.getUserId();
      // UserId can equal "" if that user has never logged in. User email will be added to the invitedModerators list, and not the moderatorList.
      if (userId == null || userId.equals("")) {
        if (this.entity.getProperty("invitedModerators") == null) {
          invitedModeratorEmails = new ArrayList<String>();
        } else {
          invitedModeratorEmails = (ArrayList) this.entity.getProperty("invitedModerators");
        }
        invitedModeratorEmails.add(email);
      }
    }
    return invitedModeratorEmails;
  }

  private ArrayList<String> findAndRetrieveModeratorList(ArrayList<String> emails) {
    ArrayList<String> moderatorIds = new ArrayList<String>();
    for(String email : emails) {
      GivrUser newUser = GivrUser.getUserByEmail(email);
      String userId = newUser.getUserId();
      // UserId can equal "" if that user has never logged in. User email will be added to the invitedModerators list, and not the moderatorList.
      if (userId != null && !userId.equals("")) {
        moderatorIds.add(newUser.getUserId());
      }
    }
    return moderatorIds;
  }

  private void updateNonFormProperties(GivrUser user, boolean forRegistration, EmbeddedEntity historyUpdate) {
    /* MillisecondSinceEpoch represent the number of milliseconds that have passed since
     * 00:00:00 UTC on January 1, 1970. It ensures that all users are entering a representation
     * of time that is independent of their time zone */
    long millisecondSinceEpoch = (long) historyUpdate.getProperty("changeTimeStampMillis");

    if(forRegistration) {
      // Setting moderatorList here instead of organizationUpdater because that will handle the form submission
      // and this servlet will handle the rest of the instantiation
      ArrayList<String> moderatorList = new ArrayList<String>();
      moderatorList.add(user.getUserId());

      /* This implementation stores history entries as embedded entities instead of custom objects
      * because it is much simpler that way */
      ArrayList changeHistory = new ArrayList<>();
      changeHistory.add(historyUpdate);

      // Upon initial creation of an organization, the list of invitedModerators will be empty.
      ArrayList<String> invitedModerators = new ArrayList<String>();

      this.entity.setProperty("creationTimeStampMillis", millisecondSinceEpoch);
      this.entity.setProperty("isApproved", false);
      this.entity.setProperty("moderatorList", moderatorList);
      this.entity.setProperty("changeHistory", changeHistory);
      this.entity.setProperty("invitedModerators", invitedModerators);
    } else {
      ArrayList<EmbeddedEntity> changeHistory = (ArrayList) this.entity.getProperty("changeHistory");
      changeHistory.add(historyUpdate);
      this.entity.setProperty("changeHistory", changeHistory);
    }
    
    this.entity.setProperty("lastEditTimeStampMillis", millisecondSinceEpoch);
  }

  public void updateInvitedModerator(GivrUser user) {
    // Called when an invited moderator logs in, will be removing their userEmail from the invitedModerators list and adding their userId to the moderatorList.
    if (this.entity.getProperty("invitedModerators") == null) {
      return;
    }
    ArrayList<String> invitedModerators = (ArrayList) this.entity.getProperty("invitedModerators");
    String userEmail = user.getUserEmail();

    if(invitedModerators.contains(userEmail)) {
      invitedModerators.remove(userEmail);
      ArrayList<String> moderatorList = (ArrayList) this.entity.getProperty("moderatorList");

      moderatorList.add(user.getUserId());
      this.entity.setProperty("moderatorList", moderatorList);
      this.entity.setProperty("invitedModerators", invitedModerators);
    }
  }

  private void updateOpenHoursProperty(HttpServletRequest request) {
    ArrayList<EmbeddedEntity> hoursOpen = new ArrayList<EmbeddedEntity>();

    for (DayOfWeek currDay : DayOfWeek.values()) {
      EmbeddedEntity dayOption = new EmbeddedEntity();
      dayOption.setProperty("day", currDay.toString());
      String isOpen = RequestHandler.getParameterOrThrow(request, currDay.toString() + "-isOpen");
      if(isOpen.equals("open")) {
        ArrayList<String> dayOptionFromTimes = new ArrayList<String>();
        ArrayList<String> dayOptionToTimes = new ArrayList<String>();
        dayOption.setProperty("isOpen", true);
        dayOptionFromTimes = RequestHandler.getParameterValuesOrThrow(request, currDay.toString() + "-from-times");
        dayOptionToTimes = RequestHandler.getParameterValuesOrThrow(request, currDay.toString() + "-to-times");

        // create from to pairs as embedded entity to support multiple time ranges for a day
        ArrayList<EmbeddedEntity> fromToPairs = ParserHelper.createFromToPairs(dayOptionFromTimes, dayOptionToTimes);
        dayOption.setProperty("fromToPairs", fromToPairs);
      } else {
        dayOption.setProperty("isOpen", false);
      }
      hoursOpen.add(dayOption);
    }

    this.entity.setProperty("dateAndHours", hoursOpen);
  }
}
