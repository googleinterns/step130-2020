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

  createOrganization(organization, isMaintainer = false) {
    const organizationElement = document.createElement("div");
    organizationElement.classList.add("organization");

    const organizationNameElement = document.createElement('div');
    organizationNameElement.classList.add("organization-name");
    organizationNameElement.textContent = organization.name;

    organizationElement.addEventListener('click', () => {
    // TODO: GetOrganizationServlet to display more information about this servlet

    // Checks if organization-popup-area element exists in index.html
    let popup_index_page = document.getElementById("organization-popup-area-index");
    if (popup_index_page) {
      popup_index_page.textContent = "";
      popup_index_page.appendChild(this.createOrganizationPopup(organization));
      popup_index_page.style.display = 'block';
    }

    // Checks if organization-popup-area element exists in edit.html
    let popup_edit_page = document.getElementById("organization-popup-area-edit");
    if (popup_edit_page) {
      popup_edit_page.textContent = "";
      popup_edit_page.appendChild(this.createOrganizationPopup(organization, isMaintainer));
      popup_edit_page.style.display = 'block';
    }
      
    });

    organizationElement.appendChild(organizationNameElement);
    return organizationElement;
  }

  createOrganizationPopup(organization, isMaintainer = false) {
    const popupElement = document.createElement("div");
    popupElement.classList.add("organization-popup");

    const popupNameElement = document.createElement('div');
    popupNameElement.classList.add("organization-name");
    popupNameElement.textContent = organization.name;

    const popupPhoneElement = document.createElement('div');
    popupPhoneElement.classList.add("organization-popup-phone");
    popupPhoneElement.textContent = organization.phone;

    const popupAddressElement = document.createElement('div');
    popupAddressElement.classList.add("organization-popup-address");
    popupAddressElement.textContent = organization.address;

    const popupEditElement = document.createElement('div');
    if (isMaintainer) {
      const popupEditButton = document.createElement('button');
      popupEditButton.classList.add("organization-edit-button");
      popupEditButton.textContent = "Edit";
      popupEditButton.addEventListener('click', () => {
        // TODO: Edit selected organization. Call fetch('/edit-organization') with params.
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
}
