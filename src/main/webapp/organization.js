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
  constructor(organization, isMaintainer = false) {
    this.organization = organization;
    this.isMaintainer = isMaintainer;
    this.organizationElement = document.createElement("div");
    this.createOrganization();
  }

  getOrganization() {
    return this.organizationElement;
  }

  createOrganization() {
    this.organizationElement.classList.add("organization");

    const organizationNameElement = document.createElement('div');
    organizationNameElement.classList.add("organization-name");
    organizationNameElement.textContent = this.organization.name;

    this.organizationElement.addEventListener('click', () => {
      // TODO: GetOrganizationServlet to display more information about this servlet

      // Checks if organization-popup-area element exists.
      let popupIndexPage = document.getElementById("organization-popup-area");
      if (popupIndexPage) {
        popupIndexPage.textContent = "";
        popupIndexPage.appendChild(this.createOrganizationPopup());
        popupIndexPage.style.display = 'block';
      }
    });

    this.organizationElement.appendChild(organizationNameElement);
  }

  createOrganizationPopup() {
    const popupElement = document.createElement("div");
    popupElement.classList.add("organization-popup");

    const popupNameElement = document.createElement('div');
    popupNameElement.classList.add("organization-name");
    popupNameElement.textContent = this.organization.name;

    const popupPhoneElement = document.createElement('div');
    popupPhoneElement.classList.add("organization-popup-phone");
    popupPhoneElement.textContent = this.organization.phone;

    const popupAddressElement = document.createElement('div');
    popupAddressElement.classList.add("organization-popup-address");
    popupAddressElement.textContent = this.organization.address;

    const popupEditElement = document.createElement('div');
    if (this.isMaintainer) {
      //TODO: Create Edit Organization Button for Maintainer.
    }

    const closeButtonElement = document.createElement('div');
    closeButtonElement.classList.add("popup-close-button");
    closeButtonElement.textContent = 'X';
    closeButtonElement.addEventListener('click', () => {
      // Remove the popup from the DOM.
      if (document.getElementById("organization-popup-area")) {
        document.getElementById("organization-popup-area").style.display = 'none';
      }
      popupElement.remove();
    });

    popupElement.appendChild(closeButtonElement);
    popupElement.appendChild(popupNameElement);
    popupElement.appendChild(popupPhoneElement);
    popupElement.appendChild(popupAddressElement);
    popupElement.appendChild(popupEditElement);
    return popupElement;
  }
}
