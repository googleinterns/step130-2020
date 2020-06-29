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

class Organization {
  Organization() {
  }

  createOrganization(organization) {
    const organizationElement = document.createElement("div");
    organizationElement.addEventListener('click', () => {
      console.log(organization.name + " has been clicked!");
      // TODO: GetOrganizationServlet to display more information about this servlet
      this.showOrganization()
    });
    organizationElement.classList.add("organization");

    const organizationNameElement = document.createElement('div');
    organizationNameElement.classList.add("organization-name");
    organizationNameElement.textContent = organization.name;

    organizationElement.appendChild(organizationNameElement);
    return organizationElement;
  }

  async showOrganization() {
    // fetch GetOrganizationServlet
  }
}
