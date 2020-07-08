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
    this.closeButtonElement = document.createElement("div");
    this.popupElement = document.createElement("div");
    this.createOrganization();
  }

  getOrganization() {
    return this.organizationElement;
  }

  createOrganization() {
    this.organizationElement.addEventListener('click', () => {
      let event = new CustomEvent('organization-selected');
      this.organizationElement.dispatchEvent(event);
    });

    this.closeButtonElement.addEventListener('click', () => {
      let event = new CustomEvent('organization-close');
      this.closeButtonElement.dispatchEvent(event);
    });

    this.organizationElement.classList.add("organization");

    const organizationNameElement = document.createElement('div');
    organizationNameElement.classList.add("organization-name");
    organizationNameElement.textContent = this.organization.name;

    this.organizationElement.appendChild(organizationNameElement);
  }

  createCloseButton() {
    this.closeButtonElement.classList.add("popup-close-button");
    this.closeButtonElement.textContent = 'X';
  }

  createOrganizationPopup() {
    this.popupElement = document.createElement("div");
    this.popupElement.classList.add("organization-popup");

    const popupNameElement = document.createElement('div');
    popupNameElement.classList.add("organization-name");
    popupNameElement.textContent = this.organization.name;

    const popupPhoneElement = document.createElement('div');
    popupPhoneElement.classList.add("organization-popup-phone");
    popupPhoneElement.textContent = this.organization.phoneNum;

    const popupAddressElement = document.createElement('div');
    popupAddressElement.classList.add("organization-popup-address");
    popupAddressElement.textContent = this.organization.address;

    const popupEditElement = document.createElement('div');
    if (this.isMaintainer) {
      //TODO: Create Edit Organization Button for Maintainer.
    }

    this.createCloseButton()

    this.popupElement.appendChild(this.closeButtonElement);
    this.popupElement.appendChild(popupNameElement);
    this.popupElement.appendChild(popupPhoneElement);
    this.popupElement.appendChild(popupAddressElement);
    this.popupElement.appendChild(popupEditElement);
    return this.popupElement;
  }
}
