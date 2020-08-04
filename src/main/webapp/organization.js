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
  constructor(organization, isMaintainer = true, forOrganizationsPage) {
    this.organization = organization;
    this.isMaintainer = isMaintainer;
    this.forOrganizationsPage = forOrganizationsPage;
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

    const organizationAddressElement = document.createElement('div');
    organizationAddressElement.classList.add("organization-address");
    organizationAddressElement.textContent = this.organization.address + ", " + 
    this.organization.city + ", " + this.organization.state + " " + this.organization.zipcode;

    const organizationPhoneElement = document.createElement('div');
    organizationPhoneElement.classList.add("organization-phone");
    organizationPhoneElement.textContent = this.organization.phoneNum;

    const organizationCategoriesElement = document.createElement("div");
    this.organization.resourceCategories.forEach((category) => {
        const categoryChip = document.createElement("div");
        categoryChip.classList.add("category-chip");
        categoryChip.textContent = category;
        organizationCategoriesElement.appendChild(categoryChip);
    })

    this.organizationElement.appendChild(organizationNameElement);
    this.organizationElement.appendChild(organizationAddressElement);
    this.organizationElement.appendChild(organizationPhoneElement);
    this.organizationElement.appendChild(organizationCategoriesElement);
  }

  createCloseButton() {
    this.closeButtonElement.classList.add("popup-close-button");
    this.closeButtonElement.textContent = 'X';
  }

  createOrganizationPopup() {

    this.popupElement = document.createElement("div");
    this.popupElement.classList.add("organization-popup");

    const popupNameElement = document.createElement('div');
    popupNameElement.classList.add("organization-popup-name");
    popupNameElement.textContent = this.organization.name;

    const popupPhoneElement = document.createElement('div');
    popupPhoneElement.classList.add("organization-popup-phone");
    popupPhoneElement.textContent = "Primary Phone: " + this.organization.phoneNum;

    const popupAddressElement = document.createElement('div');
    popupAddressElement.classList.add("organization-popup-address");
    popupAddressElement.textContent = this.organization.address + ", " + 
    this.organization.city + ", " + this.organization.state + " " + this.organization.zipcode;

    const popupEmailElement = document.createElement('div');
    popupEmailElement.classList.add("organization-popup-email");
    popupEmailElement.textContent = "Primary Email: " + this.organization.email;

    const popupWebsiteElement = document.createElement("div");
    popupWebsiteElement.textContent = "Website: ";
    const popupUrlLinkElement = document.createElement('a');
    popupUrlLinkElement.classList.add("organization-popup-url-link");
    popupUrlLinkElement.setAttribute("href", this.organization.urlLink);
    popupUrlLinkElement.textContent = this.organization.urlLink;
    popupWebsiteElement.appendChild(popupUrlLinkElement);

    const popupHoursElement = document.createElement('div');
    popupHoursElement.classList.add("organization-popup-hours");
    this.organization.hoursOpen.forEach((day) => {
      popupHoursElement.appendChild(this.createOpenHoursText(day));
    });

    const popupDescriptionElement = document.createElement('div');
    popupDescriptionElement.classList.add("organization-popup-description");
    popupDescriptionElement.textContent = "Additional Information: " + this.organization.description;

    const popupEditElement = document.createElement('button');
    if (this.forOrganizationsPage) {
      popupEditElement.classList.add("enter-button");
      popupEditElement.textContent = "Edit";
      popupEditElement.addEventListener('click', () => {
        const editFormArea = document.getElementById("edit-form-area");
        editFormArea.appendChild(this.editOrganization(this.organization));
        editFormArea.classList.remove("hide-edit-modal");
        editFormArea.classList.add("show-edit-modal")
      });
    }

    this.createCloseButton()

    this.popupElement.appendChild(this.closeButtonElement);
    this.popupElement.appendChild(popupNameElement);
    this.popupElement.appendChild(popupAddressElement);
    this.popupElement.appendChild(popupPhoneElement);
    this.popupElement.appendChild(popupEmailElement);
    this.popupElement.appendChild(popupWebsiteElement);
    this.popupElement.appendChild(popupHoursElement);
    this.popupElement.appendChild(popupDescriptionElement);
    if (this.forOrganizationsPage) {
      this.popupElement.appendChild(popupEditElement);
    }
    return this.popupElement;
  }

  editOrganization(organization) {
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

    // label and entry area for organization city
    const orgCityLabel = document.createElement("label");
    orgCityLabel.setAttribute("for", "city");
    orgCityLabel.setAttribute("id", "city-label");
    orgCityLabel.textContent = "City: ";
    editForm.appendChild(orgCityLabel);

    const orgCityEntry = document.createElement("input");
    orgCityEntry.setAttribute("type", "text");
    orgCityEntry.setAttribute("id", "city");
    orgCityEntry.setAttribute("value", `${organization.city}`);
    orgCityEntry.setAttribute("name", "org-city");
    orgCityEntry.classList.add("edit-entry");
    editForm.appendChild(orgCityEntry);

    // label and entry area for organization state
    const orgStateLabel = document.createElement("label");
    orgStateLabel.setAttribute("for", "state");
    orgStateLabel.setAttribute("id", "state-label");
    orgStateLabel.textContent = "State: ";
    editForm.appendChild(orgStateLabel);

    const orgStateEntry = document.createElement("input");
    orgStateEntry.setAttribute("type", "text");
    orgStateEntry.setAttribute("id", "state");
    orgStateEntry.setAttribute("value", `${organization.state}`);
    orgStateEntry.setAttribute("name", "org-state");
    orgStateEntry.classList.add("edit-entry");
    editForm.appendChild(orgStateEntry);
   
    // label and entry area for organization zipcode
    const orgZipcodeLabel = document.createElement("label");
    orgZipcodeLabel.setAttribute("for", "zipcode");
    orgZipcodeLabel.setAttribute("id", "zipcode-edit-label");
    orgZipcodeLabel.textContent = "Zipcode: ";
    editForm.appendChild(orgZipcodeLabel);

    const orgZipcodeEntry = document.createElement("input");
    orgZipcodeEntry.setAttribute("type", "text");
    orgZipcodeEntry.setAttribute("id", "zipcode");
    orgZipcodeEntry.setAttribute("pattern", "[0-9]{5}");
    orgZipcodeEntry.setAttribute("value", `${organization.zipcode}`);
    orgZipcodeEntry.setAttribute("name", "org-zip-code");
    orgZipcodeEntry.classList.add("edit-entry");
    editForm.appendChild(orgZipcodeEntry);

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

    const orgOpenHoursLabel = document.createElement("label");
    orgOpenHoursLabel.setAttribute("id", "hours-open-label");
    orgOpenHoursLabel.textContent = "Organization Hours: ";
    editForm.appendChild(orgOpenHoursLabel);

    const orgOpenHoursArea = document.createElement("div");
    orgOpenHoursArea.setAttribute("id", "hours-option-area");

    //creates time input options for each day
    for(let i = 0; i < 7; i++) {
        const day = organization.hoursOpen[i].propertyMap.day;
        const timeOption = new TimeOption(day, /*forRegistration*/ false, organization.hoursOpen[i], orgOpenHoursArea, /*showOpenClosedOption*/ true);
    }
    editForm.appendChild(orgOpenHoursArea);

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

    // label and entry area for organization resource category list
    const orgResourceCategoryListLabel = document.createElement("label");
    orgResourceCategoryListLabel.setAttribute("for", "org-resource-categories");
    orgResourceCategoryListLabel.setAttribute("id", "resource-category-list-label");
    orgResourceCategoryListLabel.textContent = "Resource Categories: ";
    editForm.appendChild(orgResourceCategoryListLabel);

    const orgResourceCategories = document.createElement("textarea");
    orgResourceCategories.setAttribute("type", "text");
    orgResourceCategories.setAttribute("id", "org-resource-categories");
    orgResourceCategories.setAttribute("name", "org-resource-categories");
    orgResourceCategories.classList.add("edit-entry");
    const resourceArray = organization.resourceCategories;
    orgResourceCategories.textContent = resourceArray.join(",  ");
    editForm.appendChild(orgResourceCategories);

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

    if (this.isMaintainer) {
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
    editFormSubmit.setAttribute("class", "enter-button");
    editForm.appendChild(editFormSubmit);

    editFormAreaContent.appendChild(editForm);

    return editFormAreaContent;
  }

  // Takes in array of monderator information and returns a string of moderator emails
  // joined with commas
  convertIdsToEmails(moderators) {
    let moderatorEmails = [];
    moderators.forEach((moderatorInfo) => {
        moderatorEmails.push(moderatorInfo.email);
    });
    return moderatorEmails.join(", ");
  }

  createOpenHoursText(organizationDay) {
    // organization.hoursOpen[index(1-7 for day of week)].propertyMap.
    // fromToPairs.value[index(how many set of hours for that day )].propertyMap.from/to
    const dayTimeArea = document.createElement("div");
    dayTimeArea.classList.add("day-time-area");

    const dayTimeText = document.createElement("p");
    dayTimeText.textContent = `${organizationDay.propertyMap.day}: `;

    if (organizationDay.propertyMap.isOpen) {
      let fromToString = null;
      const numPairs = organizationDay.propertyMap.fromToPairs.value.length;

      // creates from to text in the form of hh:mm - hh:mm
      for (let i = 0; i < numPairs; i++) {
        
        // parses it from 24 hour format to 12 hour format
        let from = this.parseTime(organizationDay.propertyMap.fromToPairs.value[i].propertyMap.from);
        let to = this.parseTime(organizationDay.propertyMap.fromToPairs.value[i].propertyMap.to);

        // adds a comma if it is not the last pair in the list
        fromToString = `${from} - ${to}`;
        if (numPairs - 1 != i) {
          fromToString += `, \n`;
        }
        else {
          fromToString += `\n`;
        }
        dayTimeText.textContent += fromToString;
      }
    }
    else {
      dayTimeText.textContent += `Closed`;
    }

    dayTimeArea.appendChild(dayTimeText);
    return dayTimeArea;
  }

  parseTime(time) {
    const hoursAndMinutes = time.split(":");
    let AMOrPM = null;
    let hours = parseInt(hoursAndMinutes[0]);
    let minutes = parseInt(hoursAndMinutes[1]);

    if (hours > 12) {
      AMOrPM = "PM";
      hours -= 12;
    } else if (hours == 12) {
        AMOrPM ="PM";
    } else if (hours == 0) {
        AMOrPM = "AM";
        hours = 12;
    } else {
      AMOrPM = "AM";
    }
    
    let hoursString = hours.toString();
    let minutesString = minutes.toString();
  
    if (minutes < 10) {
      minutesString = `0${minutes.toString()}`;
    }
    return `${hoursString}:${minutesString} ${AMOrPM}`;
  }
}
