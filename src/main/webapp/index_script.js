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

document.addEventListener("DOMContentLoaded", async function() {
  const organizationArea = document.getElementById('organization-list');
  renderOrganizations(organizationArea);
});

/* 
 * This async function gets a default list of organization names to display when the dom content
 * loads by not passing any parameters to /list-organizations
 */
async function renderOrganizations(organizationArea) {
  const response = await fetch(`/list-organizations`);
  const organizationNames = await response.json();
  for (let i = 0; i < organizationNames.length; i++) {
    organizationArea.appendChild(createOrganization(organizationNames[i]));
  }
}

function createOrganization(organization) {
  const organizationElement = document.createElement("div");
  organizationElement.classList.add("organization");

  const organizationNameElement = document.createElement('div');
  organizationNameElement.classList.add("organization-name");
  organizationNameElement.textContent = organization;

  organizationElement.addEventListener('click', () => {
  });

  organizationElement.appendChild(organizationNameElement);
  return organizationElement;
}
