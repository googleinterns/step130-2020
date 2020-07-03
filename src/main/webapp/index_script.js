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
  const searchArea = new SearchArea(document.getElementById('search-area'));
});

class SearchArea {
  constructor(searchAreaElement) {
    this.searchArea = searchAreaElement;
    this.organizationSearchArea = document.createElement("div");

    this.zipcodeFormArea = document.createElement("div");
    this.zipcodeForm = document.createElement("form");
    this.zipcodeForm.setAttribute("action", "/list-organizations");
    this.zipcodeForm.setAttribute("method", "POST");

    this.zipcodeFormLabel = document.createElement("label");
    this.zipcodeFormLabel.setAttribute("for", "zipcode-entry");
    this.zipcodeFormLabel.setAttribute("id", "zipcode-label");
    this.zipcodeFormLabel.textContent = "Enter a zipcode: ";
    this.zipcodeForm.appendChild(this.zipcodeFormLabel);

    this.zipcodeFormEntry = document.createElement("input");
    this.zipcodeFormEntry.setAttribute("type", "text");
    this.zipcodeFormEntry.setAttribute("id", "zipcode-entry");
    this.zipcodeFormEntry.setAttribute("pattern", "[0-9]{5}");
    this.zipcodeFormEntry.setAttribute("name", "zipcode");
    this.zipcodeForm.appendChild(this.zipcodeFormEntry);

    this.zipcodeSubmit = document.createElement("input");
    this.zipcodeSubmit.setAttribute("type", "submit");
    this.zipcodeSubmit.setAttribute("class", "gray-button");
    this.zipcodeForm.appendChild(this.zipcodeSubmit);

    this.zipcodeFormArea.appendChild(this.zipcodeForm);
    this.organizationSearchArea.appendChild(this.zipcodeForm);

    this.filterButtonArea = document.createElement("div");
    this.filterButtonArea.setAttribute("id", "filter-button-area");

    this.filterButton = document.createElement("button");
    this.filterButton.setAttribute("type", "button");
    this.filterButton.setAttribute("class", "gray-button");
    this.filterButton.setAttribute("id", "filter-button");
    this.filterButton.textContent = "Filter";
    this.filterButtonArea.appendChild(this.filterButton);

    this.organizationSearchArea.appendChild(this.filterButtonArea);

    this.organizationList = document.createElement("div");
    this.organizationList.setAttribute("id", "organization-list");
    this.requestAndDisplayOrganizations();
    this.organizationSearchArea.appendChild(this.organizationList);

    this.organizationPopupArea = document.createElement("div");
    this.organizationPopupArea.setAttribute("id", "organization-popup-area");
    this.organizationPopupArea.classList.add("hide-popup");

    this.searchArea.appendChild(this.organizationSearchArea);
    this.searchArea.appendChild(this.organizationPopupArea);
  }

   /* 
     * This async function gets a default list of organization names to display when the dom content
     * loads by not passing any parameters to /list-organizations
     */
  async requestAndDisplayOrganizations() {
    const response = await fetch(`/list-organizations`);
    const organizationNames = await response.json();
    for (let i = 0; i < organizationNames.length; i++) {
      this.organizationList.appendChild(this.createOrganization(organizationNames[i]));
    }
  }

  createOrganization(organization) {
    const organizationElement = document.createElement("div");
    organizationElement.classList.add("organization");

    const organizationNameElement = document.createElement('div');
    organizationNameElement.classList.add("organization-name");
    organizationNameElement.textContent = organization.name;

    organizationElement.addEventListener('click', () => {
      const organizationPopupArea = document.getElementById("organization-popup-area");
      organizationPopupArea.textContent = "";
      organizationPopupArea.appendChild(this.organizationPopup(organization));
      organizationPopupArea.classList.remove("hide-popup");
      organizationPopupArea.classList.add("show-popup");
    });

    organizationElement.appendChild(organizationNameElement);
    return organizationElement;
  }

  organizationPopup(organization) {
    const popupElement = document.createElement("div");
    popupElement.classList.add("organization-popup");

    const popupNameElement = document.createElement('div');
    popupNameElement.classList.add("organization-popup-name");
    popupNameElement.textContent = organization.name;

    const popupPhoneElement = document.createElement('div');
    popupPhoneElement.classList.add("organization-popup-phone");
    popupPhoneElement.textContent = organization.phoneNum;

    const popupAddressElement = document.createElement('div');
    popupAddressElement.classList.add("organization-popup-address");
    popupAddressElement.textContent = organization.address;

    const popupHoursElement = document.createElement('div');
    popupHoursElement.classList.add("organization-popup-hours");
    popupHoursElement.textContent = `Hours: ${organization.openingHour} - ${organization.closingHour}`;

    const popupEmailElement = document.createElement('div');
    popupEmailElement.classList.add("organization-popup-email");
    popupEmailElement.textContent = organization.email;

    const popupUrlLinkElement = document.createElement('div');
    popupUrlLinkElement.classList.add("organization-popup-url-link");
    popupUrlLinkElement.textContent = organization.urlLink;

    const popupDescriptionElement = document.createElement('div');
    popupDescriptionElement.classList.add("organization-popup-description");
    popupDescriptionElement.textContent = organization.description;

    const closeButtonElement = document.createElement('div');
    closeButtonElement.classList.add("popup-close-button");
    closeButtonElement.textContent = 'X';
    closeButtonElement.addEventListener('click', () => {
      // Remove the popup from the DOM.
      const organizationPopupArea = document.getElementById("organization-popup-area");
      organizationPopupArea.classList.remove("show-popup");
      organizationPopupArea.classList.add("hide-popup")
      popupElement.remove();
    });

    popupElement.appendChild(closeButtonElement);
    popupElement.appendChild(popupNameElement);
    popupElement.appendChild(popupPhoneElement);
    popupElement.appendChild(popupAddressElement);
    popupElement.appendChild(popupHoursElement);
    popupElement.appendChild(popupEmailElement);
    popupElement.appendChild(popupUrlLinkElement);
    popupElement.appendChild(popupDescriptionElement);
    return popupElement;
  }
}
