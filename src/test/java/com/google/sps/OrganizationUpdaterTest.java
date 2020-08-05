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
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class OrganizationUpdaterTest {

  private LocalServiceTestHelper helper;
  private DataHelper dataHelper;

  // @After
  // public void tearDown() {
  //   helper.tearDown();
  // }

  /**
   * Mocks request class and sets proper return values for property keys that are defined in parameterToValue HashMap.
   */
  private HttpServletRequest setMockReturnValuesAndGetRequest(HashMap<String, String> parameterToValue) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HashMap<String, String> requestParameters = new HashMap<String, String>();
    requestParameters.put("org-name", "");
    requestParameters.put("org-email", "");
    requestParameters.put("org-street-address", "");
    requestParameters.put("org-city", "");
    requestParameters.put("org-state", "");
    requestParameters.put("org-zip-code", "");
    requestParameters.put("org-phone-num", "");
    requestParameters.put("org-url", "");
    requestParameters.put("org-description", "");
    requestParameters.put("approval", "");
    requestParameters.put("moderator-list", "");
    requestParameters.put("org-resource-categories", "");
    requestParameters.put("Monday-isOpen", "closed");
    requestParameters.put("Tuesday-isOpen", "closed");
    requestParameters.put("Wednesday-isOpen", "closed");
    requestParameters.put("Thursday-isOpen", "closed");
    requestParameters.put("Friday-isOpen", "closed");
    requestParameters.put("Saturday-isOpen", "closed");
    requestParameters.put("Sunday-isOpen", "closed");

    for (Map.Entry<String,String> parameter: requestParameters.entrySet()) {
      if (parameterToValue.get(parameter.getKey()) == null) {
        when(request.getParameter(parameter.getKey())).thenReturn(parameter.getValue());
      } else {
        when(request.getParameter(parameter.getKey())).thenReturn(parameterToValue.get(parameter.getKey()));
      }
    }
    return request;
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
    HttpServletRequest request = setMockReturnValuesAndGetRequest(parameterToValue);

    boolean forRegistration = true;
    HistoryManager historyManager = new HistoryManager();
    EmbeddedEntity historyUpdate = historyManager.recordHistory("Organization 3 was registered.", 1);
    // TODO: check history manager's changeAuthorId (should be User14)

    Entity actualOrgEntity = new Entity("Distributor");
    OrganizationUpdater orgUpdater = new OrganizationUpdater(actualOrgEntity);
    GivrUser user = GivrUser.getCurrentLoggedInUser();
    try {
      orgUpdater.updateOrganization(request, user, forRegistration, historyUpdate);
    } catch (IllegalArgumentException err) {
      System.out.println("ERROR!");
    }
    Assert.assertEquals(1, 1);
  }

  /*@Test
  public void updateOrganizationForEditWithoutRightCredentialsTest() {

  }

  @Test
  public void updateOrganizationForEditWithRightCredentialsTest() {

  }*/
}
