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
    organizations.forEach((organization) => {	
      this.organizationList.appendChild(this.createOrganization(organization));
    });
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
    popupEditElement.classList.add("gray-button");
    popupEditElement.textContent = "Edit";
    popupEditElement.addEventListener('click', () => {
      const editFormArea = document.getElementById("edit-form-area");
      editFormArea.appendChild(this.editOrganization(organization));
      editFormArea.classList.remove("hide-edit-modal");
      editFormArea.classList.add("show-edit-modal")
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
    // all entry fields will be prepopulated with the current values for user experience

    //TODO(): get if user is maintainer, will determine showing approval buttons
    const isMaintainer = true;

    // TODO(): Convert user ids to emails
    const moderatorListString = this.convertIdsToEmails(organization.moderators);

    //use param list to pass in id to servlet
    const params = new URLSearchParams();
    params.append("id", organization.id);

    const editFormAreaContent = document.createElement("div");
    editFormAreaContent.setAttribute("id", "edit-form-area-content");

    const closeButtonElement = document.createElement('div');
    closeButtonElement.classList.add("popup-close-button");
    closeButtonElement.textContent = 'X';
    closeButtonElement.addEventListener('click', () => {
      // Remove the popup from the DOM.
      const editFormArea = document.getElementById("edit-form-area");
      editFormArea.classList.remove("show-edit-modal");
      editFormArea.classList.add("hide-edit-modal")
      editFormAreaContent.remove();
    });
    editFormAreaContent.appendChild(closeButtonElement);

    // create edit form element
    const editForm = document.createElement("form");
    editForm.setAttribute("action", `/edit-organization?${params.toString()}`);
    editForm.setAttribute("method", "POST");

    // label and entry area for organization name
    const orgNameLabel = document.createElement("label");
    orgNameLabel.setAttribute("for", "name");
    orgNameLabel.setAttribute("id", "name-label");
    orgNameLabel.textContent = "Organization Name: ";
    editForm.appendChild(orgNameLabel);

    const orgNameEntry = document.createElement("input");
    orgNameEntry.setAttribute("type", "text");
    orgNameEntry.setAttribute("id", "name");
    orgNameEntry.setAttribute("value", `${organization.name}`);
    orgNameEntry.setAttribute("name", "org-name");
    orgNameEntry.classList.add("edit-entry");
    editForm.appendChild(orgNameEntry);

    // label and entry area for organization email
    const orgEmailLabel = document.createElement("label");
    orgEmailLabel.setAttribute("for", "email");
    orgEmailLabel.setAttribute("id", "email-label");
    orgEmailLabel.textContent = "Email: ";
    editForm.appendChild(orgEmailLabel);

    const orgEmailEntry = document.createElement("input");
    orgEmailEntry.setAttribute("type", "text");
    orgEmailEntry.setAttribute("id", "email");
    orgEmailEntry.setAttribute("value", `${organization.email}`);
    orgEmailEntry.setAttribute("name", "org-email");
    orgEmailEntry.classList.add("edit-entry");
    editForm.appendChild(orgEmailEntry);

    // label and entry area for organization address
    const orgAddressLabel = document.createElement("label");
    orgAddressLabel.setAttribute("for", "address");
    orgAddressLabel.setAttribute("id", "address-label");
    orgAddressLabel.textContent = "Address: ";
    editForm.appendChild(orgAddressLabel);

    const orgAddressEntry = document.createElement("input");
    orgAddressEntry.setAttribute("type", "text");
    orgAddressEntry.setAttribute("id", "address");
    orgAddressEntry.setAttribute("value", `${organization.address}`);
    orgAddressEntry.setAttribute("name", "org-street-address");
    orgAddressEntry.classList.add("edit-entry");
    editForm.appendChild(orgAddressEntry);

    // label and entry area for organization phone
    const orgPhoneLabel = document.createElement("label");
    orgPhoneLabel.setAttribute("for", "phone");
    orgPhoneLabel.setAttribute("id", "phone-label");
    orgPhoneLabel.textContent = "Phone: ";
    editForm.appendChild(orgPhoneLabel);

    const orgPhoneEntry = document.createElement("input");
    orgPhoneEntry.setAttribute("type", "text");
    orgPhoneEntry.setAttribute("id", "phone-num");
    orgPhoneEntry.setAttribute("pattern", "[0-9]{10}");
    orgPhoneEntry.setAttribute("value", `${organization.phoneNum}`);
    orgPhoneEntry.setAttribute("name", "org-phone-num");
    orgPhoneEntry.classList.add("edit-entry");
    editForm.appendChild(orgPhoneEntry);

    // label and entry area for organization url-link
    const orgUrlLinkLabel = document.createElement("label");
    orgUrlLinkLabel.setAttribute("for", "url-link");
    orgUrlLinkLabel.setAttribute("id", "url-link-label");
    orgUrlLinkLabel.textContent = "URL Link: ";
    editForm.appendChild(orgUrlLinkLabel);

    const orgUrlLinkEntry = document.createElement("input");
    orgUrlLinkEntry.setAttribute("type", "text");
    orgUrlLinkEntry.setAttribute("id", "url-link");
    orgUrlLinkEntry.setAttribute("value", `${organization.urlLink}`);
    orgUrlLinkEntry.setAttribute("name", "org-url");
    orgUrlLinkEntry.classList.add("edit-entry");
    editForm.appendChild(orgUrlLinkEntry);

    // label and entry area for organization description
    const orgDescriptionLabel = document.createElement("label");
    orgDescriptionLabel.setAttribute("for", "description");
    orgDescriptionLabel.setAttribute("id", "description-label");
    orgDescriptionLabel.textContent = "Description: ";
    editForm.appendChild(orgDescriptionLabel);

    const orgDescriptionEntry = document.createElement("textarea");
    orgDescriptionEntry.setAttribute("type", "text");
    orgDescriptionEntry.setAttribute("id", "description");
    orgDescriptionEntry.setAttribute("name", "org-description");
    orgDescriptionEntry.classList.add("edit-entry");
    orgDescriptionEntry.textContent = organization.description;
    editForm.appendChild(orgDescriptionEntry);

    // label and entry area for organization moderator list
    const orgModeratorListLabel = document.createElement("label");
    orgModeratorListLabel.setAttribute("for", "moderator-list");
    orgModeratorListLabel.setAttribute("id", "moderator-list-label");
    orgModeratorListLabel.textContent = "Moderator List: ";
    editForm.appendChild(orgModeratorListLabel);

    const orgModeratorListEntry = document.createElement("textarea");
    orgModeratorListEntry.setAttribute("type", "text");
    orgModeratorListEntry.setAttribute("id", "moderator-list");
    orgModeratorListEntry.setAttribute("name", "moderator-list");
    orgModeratorListEntry.classList.add("edit-entry");
    orgModeratorListEntry.textContent = moderatorListString;
    editForm.appendChild(orgModeratorListEntry);

    if (isMaintainer) {
      // label and approval buttons for maintainer approval
      const approvedLabel = document.createElement("label");
      approvedLabel.setAttribute("for", "approved");
      approvedLabel.setAttribute("id", "approved-label");
      approvedLabel.textContent = "Approved: ";
      editForm.appendChild(approvedLabel);

      const approvedButton = document.createElement("input");
      approvedButton.setAttribute("type", "radio");
      approvedButton.setAttribute("id", "approved");
      approvedButton.setAttribute("value", "approved");
      approvedButton.setAttribute("name", "approval");
      if (organization.isApproved == true) {
        approvedButton.setAttribute("checked", "checked");
      }
      editForm.appendChild(approvedButton);
      
      // page break for styling purposes
      editForm.appendChild(document.createElement("br"));

      const notApprovedLabel = document.createElement("label");
      notApprovedLabel.setAttribute("for", "notApproved");
      notApprovedLabel.setAttribute("id", "not-approved-label");
      notApprovedLabel.textContent = "Not Approved: ";
      editForm.appendChild(notApprovedLabel)

      const notApprovedButton = document.createElement("input");
      notApprovedButton.setAttribute("type", "radio");
      notApprovedButton.setAttribute("id", "notApproved");
      notApprovedButton.setAttribute("value", "notApproved");
      notApprovedButton.setAttribute("name", "approval");
      if (organization.isApproved == false) {
        notApprovedButton.setAttribute("checked", "checked");
      }
      editForm.appendChild(notApprovedButton);

      editForm.appendChild(document.createElement("br"));
    }

    const editFormSubmit = document.createElement("input");
    editFormSubmit.setAttribute("type", "submit");
    editFormSubmit.setAttribute("class", "gray-button");
    editForm.appendChild(editFormSubmit);

    editFormAreaContent.appendChild(editForm);

    return editFormAreaContent;
  }

  convertIdsToEmails(moderators) {
      return "placeholder";
  }
}
