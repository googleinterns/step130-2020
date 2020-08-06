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

package com.google.sps;

import static org.mockito.Mockito.*;

import com.google.sps.data.DataHelper;
import com.google.sps.data.HistoryManager;
import com.google.sps.data.OrganizationUpdater;
import com.google.sps.data.GivrUser;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import javax.servlet.http.HttpServletRequest;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.logging.Logger;
import java.util.logging.Level;

/** */
@RunWith(JUnit4.class)
public final class OrganizationUpdaterTest {

  private LocalServiceTestHelper helper;
  private DataHelper dataHelper;
  private static Logger logger = Logger.getLogger("OrganizationUpdaterTest Logger");

  private HashMap<String, String> setUpAndReturnRequestParameterMap() {
    HashMap<String, String> requestParameters = new HashMap<String, String>();
    requestParameters.put("org-name", "");
    requestParameters.put("org-email", "");
    requestParameters.put("org-street-address", "tempAddress");
    requestParameters.put("org-city", "tempCity");
    requestParameters.put("org-state", "tempState");
    requestParameters.put("org-zip-code", "tempZipcode");
    requestParameters.put("org-phone-num", "0000000000");
    requestParameters.put("org-url", "google.com");
    requestParameters.put("org-description", "tempDescription");
    requestParameters.put("approval", "notApproved");
    requestParameters.put("moderator-list", "");
    requestParameters.put("org-resource-categories", "");
    requestParameters.put("Monday-isOpen", "closed");
    requestParameters.put("Tuesday-isOpen", "closed");
    requestParameters.put("Wednesday-isOpen", "closed");
    requestParameters.put("Thursday-isOpen", "closed");
    requestParameters.put("Friday-isOpen", "closed");
    requestParameters.put("Saturday-isOpen", "closed");
    requestParameters.put("Sunday-isOpen", "closed");

    return requestParameters;
  }

  /**
   * Mocks request class and sets proper return values for property keys that are defined in parameterToValue HashMap.
   */
  private HttpServletRequest setMockReturnValuesAndGetRequest(HashMap<String, String> parameterToValue) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HashMap<String, String> requestParameters = setUpAndReturnRequestParameterMap();

    for (Map.Entry<String,String> parameter: requestParameters.entrySet()) {
      if (parameterToValue.get(parameter.getKey()) == null) {
        when(request.getParameter(parameter.getKey())).thenReturn(parameter.getValue());
      } else {
        when(request.getParameter(parameter.getKey())).thenReturn(parameterToValue.get(parameter.getKey()));
      }
    }
    return request;
  }

  private boolean checkIfOrgEntitiesEqual(Entity expectedOrgEntity, Entity actualOrgEntity) {
    HashSet<String> propertyKeys = new HashSet<String>();
    propertyKeys.add("name");
    propertyKeys.add("email");
    propertyKeys.add("streetAddress");
    propertyKeys.add("city");
    propertyKeys.add("state");
    propertyKeys.add("zipcode");
    propertyKeys.add("phone");
    propertyKeys.add("url");
    propertyKeys.add("description");
    propertyKeys.add("isApproved");
    propertyKeys.add("moderatorList");
    propertyKeys.add("resourceCategories");

    for (String propertyKey: propertyKeys) {
      if (propertyKey.equals("moderatorList") || propertyKey.equals("resourceCategories")) {
        // moderatorList and resourceCategories are of type ArrayList when placed into Datastore.
        ArrayList expectedList = (ArrayList) expectedOrgEntity.getProperty(propertyKey);
        ArrayList actualList = (ArrayList) actualOrgEntity.getProperty(propertyKey);
        for (int i = 0; i < expectedList.size(); i++) {
          if (!expectedList.get(i).equals(actualList.get(i))) {
            return false;
          }
        }
        continue;
      }
      if (!expectedOrgEntity.getProperty(propertyKey).equals(actualOrgEntity.getProperty(propertyKey))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests updateOrganization() when called for registering. Anyone can register a new Organization, as long as the required request form fields are filled out.
   */
  @Test
  public void updateOrganizationForRegistrationTest() {
    dataHelper = new DataHelper();
    // User who is requesting to register "Org3" has Id of "User14" and is NOT a Maintainer. User should not exist in the Datastore.
    helper = dataHelper.setUpAndReturnLocalServiceTestHelper(false /* userIsMaintainer */, "User14" /* userId */);

    HashMap<String, String> parameterToValue = new HashMap<String, String>();
    parameterToValue.put("org-name", "Org3");
    // User should also be added to the Datastore, TODO: so we can check for that.
    parameterToValue.put("moderator-list", "baikj+test14@google.com");
    parameterToValue.put("org-email", "org3+test14@google.com");
    parameterToValue.put("org-resource-categories", "Food");
    HttpServletRequest request = setMockReturnValuesAndGetRequest(parameterToValue);

    boolean forRegistration = true;
    HistoryManager historyManager = new HistoryManager();
    EmbeddedEntity historyUpdate = historyManager.recordHistory("Organization 3 was registered.", 1 /* millisecondsSinceEpoch */);

    Entity actualOrgEntity = new Entity("Distributor");
    OrganizationUpdater orgUpdater = new OrganizationUpdater(actualOrgEntity);
    GivrUser user = GivrUser.getCurrentLoggedInUser();
    try {
      orgUpdater.updateOrganization(request, user, forRegistration, historyUpdate);
    } catch (IllegalArgumentException err) {
      logger.log(Level.SEVERE, "ERROR: OrganizationUpdater's updateOrganization had an error.");
      throw new IllegalArgumentException();
    }

    ArrayList<String> expectedModeratorList = new ArrayList<String>();
    expectedModeratorList.add("User14");
    ArrayList<String> expectedResourceCategories = new ArrayList<String>();
    expectedResourceCategories.add("Food");
    Entity expectedOrgEntity = new Entity("Distributor");
    expectedOrgEntity.setProperty("name", "Org3");
    expectedOrgEntity.setProperty("email", "org3+test14@google.com");
    expectedOrgEntity.setProperty("streetAddress", "tempAddress");
    expectedOrgEntity.setProperty("city", "tempCity");
    expectedOrgEntity.setProperty("state", "tempState");
    expectedOrgEntity.setProperty("zipcode", "tempZipcode");
    expectedOrgEntity.setProperty("phone", "0000000000");
    expectedOrgEntity.setProperty("url", "google.com");
    expectedOrgEntity.setProperty("description", "tempDescription");
    expectedOrgEntity.setProperty("isApproved", false);
    expectedOrgEntity.setProperty("moderatorList", expectedModeratorList);
    expectedOrgEntity.setProperty("resourceCategories", expectedResourceCategories);

    Assert.assertTrue(checkIfOrgEntitiesEqual(expectedOrgEntity, actualOrgEntity));
  }

  /**
   * Tests user attempting to update an Organization that they do not moderate.
   */
  @Test(expected = IllegalArgumentException.class)
  public void updateOrganizationForEditWithoutRightCredentialsTest() {
    // User5 is NOT a moderator. Updating an Organization without the right credentials will fail.
    dataHelper = new DataHelper();
    helper = dataHelper.setUpAndReturnLocalServiceTestHelper(false /* userIsMaintainer */, "User5" /* userId */);

    boolean forRegistration = false;
    HistoryManager historyManager = new HistoryManager();
    EmbeddedEntity historyUpdate = historyManager.recordHistory("Organization is updated.", 1 /* millisecondsSinceEpoch */);

    HttpServletRequest request = mock(HttpServletRequest.class);
    Entity actualOrgEntity = new Entity("Distributor");
    OrganizationUpdater orgUpdater = new OrganizationUpdater(actualOrgEntity);
    GivrUser user = GivrUser.getCurrentLoggedInUser();
    try {
      orgUpdater.updateOrganization(request, user, forRegistration, historyUpdate);
    } catch (IllegalArgumentException err) {
      logger.log(Level.SEVERE, "ERROR: OrganizationUpdater's updateOrganization had an error.");
      throw new IllegalArgumentException();
    }
  }

  @Test
  public void updateOrganizationForEditWithRightCredentialsTest() {

  }
}
