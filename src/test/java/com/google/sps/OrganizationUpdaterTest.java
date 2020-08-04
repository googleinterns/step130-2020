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
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import javax.servlet.http.HttpServletRequest;

public final class OrganizationUpdaterTest {

  private LocalServiceTestHelper helper;

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /**
   * Tests updateOrganization() when called for registering. Anyone can register a new Organization, as long as the required request form fields are filled out.
   */
  @Test
  public void updateOrganizationForRegistrationTest() {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getParameter("org-name")).thenReturn("");
    when(request.getParameter("org-email")).thenReturn("");
    when(request.getParameter("org-street-address")).thenReturn("");
    when(request.getParameter("org-city")).thenReturn("");
    when(request.getParameter("org-state")).thenReturn("");
    when(request.getParameter("org-zip-code")).thenReturn("");
    when(request.getParameter("org-phone-num")).thenReturn("");
    when(request.getParameter("org-url")).thenReturn("");
    when(request.getParameter("org-description")).thenReturn("");
    when(request.getParameter("approval")).thenReturn("");
    when(request.getParameter("moderator-list")).thenReturn("");
    when(request.getParameter("org-resource-categories")).thenReturn("");

  }

  @Test
  public void updateOrganizationForEditWithoutRightCredentialsTest() {

  }

  @Test
  public void updateOrganizationForEditWithRightCredentialsTest() {

  }
}
