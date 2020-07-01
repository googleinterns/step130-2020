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
    this.zipcodeFormArea.setAttribute("id", "zipcode-form-area");

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
    //TODO: fetch organizations with param
    const response = await fetch(`/list-organizations`);
    const organizations = await response.json();
    for (let i = 0; i < organizations.length; i++) {
      this.organizationList.appendChild(this.createOrganization(organizations[i]));
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
    popupNameElement.classList.add("organization-name");
    popupNameElement.textContent = organization.name;

    const popupPhoneNumElement = document.createElement('div');
    popupPhoneNumElement.classList.add("organization-popup-phoneNum");
    popupPhoneNumElement.textContent = organization.phoneNum;

    const popupAddressElement = document.createElement('div');
    popupAddressElement.classList.add("organization-popup-address");
    popupAddressElement.textContent = organization.address;

    const popupEditElement = document.createElement('button');
    popupEditElement.textContent = "Edit";
    popupEditElement.addEventListener('click', () => {
        popupElement.appendChild(this.editOrganization(organization));
    });

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
    popupElement.appendChild(popupPhoneNumElement);
    popupElement.appendChild(popupAddressElement);
    popupElement.appendChild(popupEditElement);
    return popupElement;
  }
  
  editOrganization(organization) {
    //use param list to pass in id to servlet
    const params = new URLSearchParams();
    params.append("id", organization.id);  
    this.editFormArea = document.createElement("div");

    // create edit form element
    this.editForm = document.createElement("form");
    this.editForm.setAttribute("action", `/edit-organization?${params.toString()}`);
    this.editForm.setAttribute("method", "POST");

    // label and entry area for organization name
    this.orgNameLabel = document.createElement("label");
    this.orgNameLabel.setAttribute("for", "name");
    this.orgNameLabel.setAttribute("id", "name-label");
    this.orgNameLabel.textContent = "Organization Name: ";
    this.editForm.appendChild(this.orgNameLabel);

    this.orgNameEntry = document.createElement("input");
    this.orgNameEntry.setAttribute("type", "text");
    this.orgNameEntry.setAttribute("id", "name");
    this.orgNameEntry.setAttribute("value", `${organization.name}`);
    this.orgNameEntry.setAttribute("name", "org-name");
    this.editForm.appendChild(this.orgNameEntry);

    // label and entry area for organization email
    this.orgEmailLabel = document.createElement("label");
    this.orgEmailLabel.setAttribute("for", "email");
    this.orgEmailLabel.setAttribute("id", "email-label");
    this.orgEmailLabel.textContent = "Email: ";
    this.editForm.appendChild(this.orgEmailLabel);

    this.orgEmailEntry = document.createElement("input");
    this.orgEmailEntry.setAttribute("type", "text");
    this.orgEmailEntry.setAttribute("id", "email");
    this.orgEmailEntry.setAttribute("value", `${organization.email}`);
    this.orgEmailEntry.setAttribute("name", "email");
    this.editForm.appendChild(this.orgEmailEntry);

    // label and entry area for organization address
    this.orgAddressLabel = document.createElement("label");
    this.orgAddressLabel.setAttribute("for", "address");
    this.orgAddressLabel.setAttribute("id", "address-label");
    this.orgAddressLabel.textContent = "Address: ";
    this.editForm.appendChild(this.orgAddressLabel);

    this.orgAddressEntry = document.createElement("input");
    this.orgAddressEntry.setAttribute("type", "text");
    this.orgAddressEntry.setAttribute("id", "address");
    this.orgAddressEntry.setAttribute("value", `${organization.address}`);
    this.orgAddressEntry.setAttribute("name", "address");
    this.editForm.appendChild(this.orgAddressEntry);

    // label and entry area for organization phone
    this.orgPhoneLabel = document.createElement("label");
    this.orgPhoneLabel.setAttribute("for", "phone");
    this.orgPhoneLabel.setAttribute("id", "phone-label");
    this.orgPhoneLabel.textContent = "Phone: ";
    this.editForm.appendChild(this.orgPhoneLabel);

    this.orgPhoneEntry = document.createElement("input");
    this.orgPhoneEntry.setAttribute("type", "text");
    this.orgPhoneEntry.setAttribute("id", "phone-number");
    this.orgPhoneEntry.setAttribute("pattern", "[0-9]{10}");
    this.orgPhoneEntry.setAttribute("value", `${organization.phoneNum}`);
    this.orgPhoneEntry.setAttribute("name", "phone-number");
    this.editForm.appendChild(this.orgPhoneEntry);

    // label and entry area for organization hour open
    this.orgHourOpenLabel = document.createElement("label");
    this.orgHourOpenLabel.setAttribute("for", "hour-open");
    this.orgHourOpenLabel.setAttribute("id", "hour-open-label");
    this.orgHourOpenLabel.textContent = "Hour Open: ";
    this.editForm.appendChild(this.orgHourOpenLabel);

    this.orgHourOpenEntry = document.createElement("input");
    this.orgHourOpenEntry.setAttribute("type", "number");
    this.orgHourOpenEntry.setAttribute("min", "0");
    this.orgHourOpenEntry.setAttribute("max", "23");
    this.orgHourOpenEntry.setAttribute("id", "hour-open");
    this.orgHourOpenEntry.setAttribute("value", `${organization.openingHour}`);
    this.orgHourOpenEntry.setAttribute("name", "hour-open");
    this.editForm.appendChild(this.orgHourOpenEntry);

    // label and entry area for organization hour closed
    this.orgHourClosedLabel = document.createElement("label");
    this.orgHourClosedLabel.setAttribute("for", "hour-closed");
    this.orgHourClosedLabel.setAttribute("id", "hour-closed-label");
    this.orgHourClosedLabel.textContent = "Hour Closed: ";
    this.editForm.appendChild(this.orgHourClosedLabel);

    this.orgHourClosedEntry = document.createElement("input");
    this.orgHourClosedEntry.setAttribute("type", "number");
    this.orgHourClosedEntry.setAttribute("min", "0");
    this.orgHourClosedEntry.setAttribute("max", "23");
    this.orgHourClosedEntry.setAttribute("id", "hour-closed");
    this.orgHourClosedEntry.setAttribute("value", `${organization.closingHour}`);
    this.orgHourClosedEntry.setAttribute("name", "hour-closed");
    this.editForm.appendChild(this.orgHourClosedEntry);

    // label and entry area for organization url-link
    this.orgUrlLinkLabel = document.createElement("label");
    this.orgUrlLinkLabel.setAttribute("for", "url-link");
    this.orgUrlLinkLabel.setAttribute("id", "url-link-label");
    this.orgUrlLinkLabel.textContent = "URL Link: ";
    this.editForm.appendChild(this.orgUrlLinkLabel);

    this.orgUrlLinkEntry = document.createElement("input");
    this.orgUrlLinkEntry.setAttribute("type", "text");
    this.orgUrlLinkEntry.setAttribute("id", "url-link");
    this.orgUrlLinkEntry.setAttribute("value", `${organization.urlLink}`);
    this.orgUrlLinkEntry.setAttribute("name", "url-link");
    this.editForm.appendChild(this.orgUrlLinkEntry);

    // label and entry area for organization description
    this.orgDescriptionLabel = document.createElement("label");
    this.orgDescriptionLabel.setAttribute("for", "description");
    this.orgDescriptionLabel.setAttribute("id", "description-label");
    this.orgDescriptionLabel.textContent = "Description: ";
    this.editForm.appendChild(this.orgDescriptionLabel);

    this.orgDescriptionEntry = document.createElement("textarea");
    this.orgDescriptionEntry.setAttribute("type", "text");
    this.orgDescriptionEntry.setAttribute("id", "description");
    this.orgDescriptionEntry.setAttribute("value", `${organization.description}`);
    this.orgDescriptionEntry.setAttribute("name", "description");
    this.editForm.appendChild(this.orgDescriptionEntry);

    this.editFormSubmit = document.createElement("input");
    this.editFormSubmit.setAttribute("type", "submit");
    this.editFormSubmit.setAttribute("class", "gray-button");
    this.editForm.appendChild(this.editFormSubmit);

    this.editFormArea.appendChild(this.editForm);

    return this.editFormArea;
  }
}

