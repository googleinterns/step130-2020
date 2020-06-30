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

      // Checks if organization-popup-area element exists in index.html
      let popupIndexPage = document.getElementById("organization-popup-area-index");
      if (popupIndexPage) {
        popupIndexPage.textContent = "";
        popupIndexPage.appendChild(this.createOrganizationPopup());
        popupIndexPage.style.display = 'block';
      }

      // Checks if organization-popup-area element exists in edit.html
      let popupEditPage = document.getElementById("organization-popup-area-edit");
      if (popupEditPage) {
        popupEditPage.textContent = "";
        popupEditPage.appendChild(this.createOrganizationPopup());
        popupEditPage.style.display = 'block';
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
      const popupEditButton = document.createElement('button');
      popupEditButton.classList.add("organization-edit-button");
      popupEditButton.textContent = "Edit";
      popupEditButton.addEventListener('click', () => {
        popupElement.appendChild(this.editOrganization());
      });
      popupElement.appendChild(popupEditButton);
    }

    const closeButtonElement = document.createElement('div');
    closeButtonElement.classList.add("popup-close-button");
    closeButtonElement.textContent = 'X';
    closeButtonElement.addEventListener('click', () => {
      // Remove the popup from the DOM.
      if (document.getElementById("organization-popup-area-edit")) {
        document.getElementById("organization-popup-area-edit").style.display = 'none';
      }
      if (document.getElementById("organization-popup-area-index")) {
        document.getElementById("organization-popup-area-index").style.display = 'none';
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
  // TODO: use this.organization to send/edit selected organization by this.organization.id. Call fetch('/edit-organization') with params.
  editOrganization() {
    //use param list to pass in id to servlet
    const params = new URLSearchParams();
    params.append("id", this.organization.id);  
    this.editFormArea = document.createElement("div");
 
    // create edit form element
    this.editForm = document.createElement("form");
    this.editForm.setAttribute("action", "/edit-organizations");
    this.editForm.setAttribute("method", "POST");
    this.editForm.setAttribute("body", params);
 
    // label and entry area for organization name
    this.orgNameLabel = document.createElement("label");
    this.orgNameLabel.setAttribute("for", "name");
    this.orgNameLabel.setAttribute("id", "name-label");
    this.orgNameLabel.textContent = "Organization Name: ";
    this.editForm.appendChild(this.orgNameLabel);
 
    this.orgNameEntry = document.createElement("input");
    this.orgNameEntry.setAttribute("type", "text");
    this.orgNameEntry.setAttribute("id", "name");
    this.orgNameEntry.setAttribute("placeholder", "Name");
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
    this.orgEmailEntry.setAttribute("placeholder", "Email");
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
    this.orgAddressEntry.setAttribute("placeholder", "Street address");
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
    this.orgPhoneEntry.setAttribute("id", "phone");
    this.orgPhoneEntry.setAttribute("pattern", "[0-9]{10}");
    this.orgPhoneEntry.setAttribute("placeholder", "phone");
    this.orgPhoneEntry.setAttribute("name", "phone");
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
    this.orgHourOpenEntry.setAttribute("min", "23");
    this.orgHourOpenEntry.setAttribute("id", "hour-open");
    this.orgHourOpenEntry.setAttribute("placeholder", "Hour open");
    this.orgHourOpenEntry.setAttribute("name", "hour-open");
    this.editForm.appendChild(this.orgHourOpenEntry);
 
    // label and entry area for organization hour closed
    this.orgHourClosedLabel = document.createElement("label");
    this.orgHourClosedLabel.setAttribute("for", "hour-open");
    this.orgHourClosedLabel.setAttribute("id", "hour-open-label");
    this.orgHourClosedLabel.textContent = "Hour Closed: ";
    this.editForm.appendChild(this.orgHourClosedLabel);
 
    this.orgHourClosedEntry = document.createElement("input");
    this.orgHourClosedEntry.setAttribute("type", "number");
    this.orgHourClosedEntry.setAttribute("min", "0");
    this.orgHourClosedEntry.setAttribute("min", "23");
    this.orgHourClosedEntry.setAttribute("id", "hour-closed");
    this.orgHourClosedEntry.setAttribute("placeholder", "Hour closed");
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
    this.orgUrlLinkEntry.setAttribute("placeholder", "url-link");
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
    this.orgDescriptionEntry.setAttribute("placeholder", "Org description");
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
