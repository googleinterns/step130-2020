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

document.addEventListener('DOMContentLoaded', () => {
  // document.getElementById('approved-organization-list');
  // document.getElementById('not-approved-organization-list');

  // TODO: display approved list ONLY if User is a Moderator.
  
  // TODO: display not-approved list if User is a Maintainer.

  // TODO: request for a list of organizations. Depending on User's status, will display different lists.
  let organizations = [{
    "name": "Organization A",
    "address": "123 Billy Bur Way, FakeCity 00000",
    "phone": "123-456-7890"
  },
  {
    "name": "Organization B",
    "address": "123 Billy Bur Way, FakeCity 00000",
    "phone": "123-456-7890"
  },
  {
    "name": "Organization C",
    "address": "123 Billy Bur Way, FakeCity 00000",
    "phone": "123-456-7890"
  }];

  const organizationArea = document.getElementById("all-organizations");
  // organizationArea.setAttribute(")
  // TODO: Change isMaintainer or isModerator based on User status.
  let isMaintainer = true;
  let organizationPopupArea = document.createElement("div");
  organizationPopupArea.setAttribute("id", "organization-popup-area-edit");
  
  organizations.forEach((organization) => {
    const newOrganization = new Organization();
    document.getElementById('approved-organization-list').appendChild(newOrganization.createOrganization(organization, isMaintainer));
  });

  organizationArea.appendChild(organizationPopupArea);
});
