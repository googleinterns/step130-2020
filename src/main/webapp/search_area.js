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
  // TODO: Get Maintainer status by checking if requester User is a Maintainer. 
  // This checks that requester User has valid credentials to edit/delete/view ALL organizations. (by checking userID)
  let isMaintainer = false;

  // TODO: Create separate JS file for listing organizations, instead of using SearchArea and having these checks below
  if (document.getElementById('search-area')) {
    const mainSearchArea = new SearchArea(document.getElementById('search-area'), isMaintainer);
    await mainSearchArea.getListOfOrganizations();
    await mainSearchArea.renderListOfOrganizations()
  }
  isMaintainer = true;
  if (document.getElementById('all-organizations')) {
    const organizationSearchArea = new SearchArea(document.getElementById('all-organizations'), isMaintainer);
    await organizationSearchArea.getListOfOrganizations();
    await organizationSearchArea.renderListOfOrganizations();
  }
});

class SearchArea {
  constructor(searchAreaElement, organizations, isMaintainer) {
    this.searchArea = searchAreaElement;
    this.organizationSearchArea = document.createElement("div");
    this.isMaintainer = isMaintainer;
    this.filterParams = new URLSearchParams();
    this.organizationObjectsList = [];

    this.zipcodeFormArea = document.createElement("div");
    this.form = document.createElement("form");
    this.form.setAttribute("onsubmit", "return false");

    this.zipcodeFormLabel = document.createElement("label");
    this.zipcodeFormLabel.setAttribute("for", "zipcode-entry");
    this.zipcodeFormLabel.setAttribute("id", "zipcode-label");
    this.zipcodeFormLabel.textContent = "Enter a zipcode: ";
    this.form.appendChild(this.zipcodeFormLabel);

    this.zipcodeFormEntry = document.createElement("input");
    this.zipcodeFormEntry.setAttribute("type", "text");
    this.zipcodeFormEntry.setAttribute("id", "zipcode-entry");
    this.zipcodeFormEntry.setAttribute("pattern", "[0-9]{5}");
    this.zipcodeFormEntry.setAttribute("name", "zipcode");
    this.form.appendChild(this.zipcodeFormEntry);

    this.zipcodeSubmit = document.createElement("input");
    this.zipcodeSubmit.setAttribute("type", "submit");
    this.zipcodeSubmit.setAttribute("class", "gray-button");
    this.zipcodeSubmit.addEventListener('click', () => this.setUrlParamValue("zipcode", this.form.zipcode.value));
    this.form.appendChild(this.zipcodeSubmit);

    this.zipcodeFormArea.appendChild(this.form);
    this.organizationSearchArea.appendChild(this.zipcodeFormArea);

    this.filterInputArea = document.createElement("input");
    this.filterInputArea.setAttribute("list", "filter-datalist");
    this.filterInputArea.setAttribute("id", "filter-input-area");
    this.filterInputArea.setAttribute("placeholder", "Filter Results");
    this.filterInputArea.addEventListener('keypress', function (e) {
      if (e.key === 'Enter') {
        this.setUrlParamValue("filterParam", this.filterInputArea.value);
      }
    }.bind(this));

    this.filterDataList = document.createElement("datalist");
    this.filterDataList.setAttribute("id", "filter-datalist");
    this.filterOptions = ["Foods", "Clothing", "Shelter"];
    for (let i = 0; i < this.filterOptions.length; i++) {
      const option = document.createElement("option");
      option.value = this.filterOptions[i];
      this.filterDataList.appendChild(option);
    }

    this.filterInputArea.appendChild(this.filterDataList);
    this.organizationSearchArea.appendChild(this.filterInputArea);

    this.organizationListArea = document.createElement("div");
    this.organizationListArea.setAttribute("id", "organization-list");
    
    this.organizationPopupArea = document.createElement("div");
    this.organizationPopupArea.setAttribute("id", "organization-popup-area");
    this.organizationPopupArea.classList.add("hide-popup");

    this.organizationSearchArea.appendChild(this.organizationListArea);
    this.searchArea.appendChild(this.organizationSearchArea);
    this.searchArea.appendChild(this.organizationPopupArea);
  }

  async renderListOfOrganizations() {
    this.organizationObjectsList.forEach((organization) => {
      const newOrganization = new Organization(organization, this.isMaintainer);

      newOrganization.organizationElement.addEventListener('organization-selected', () => {
        const organizationPopupArea = document.getElementById("organization-popup-area");
        organizationPopupArea.textContent = "";
        organizationPopupArea.appendChild(newOrganization.createOrganizationPopup());
        organizationPopupArea.classList.add("show-popup");
        organizationPopupArea.classList.remove("hide-popup");
      });

      newOrganization.closeButtonElement.addEventListener('organization-close', () => {
      //  Remove the popup from the DOM.
        document.getElementById("organization-popup-area").classList.add("hide-popup");
        document.getElementById("organization-popup-area").classList.remove("show-popup");
        newOrganization.popupElement.remove();
      });

      this.organizationListArea.appendChild(newOrganization.getOrganization());
    });
  }

  async getListOfOrganizations() {
    let response;
    if (this.filterParams) {
      response = await fetch(`/list-organizations?${this.filterParams.toString()}`);
    } else {
      response = await fetch(`/list-organizations`);
    }
    this.organizationObjectsList = await response.json();
  }

  async setUrlParamValue(urlParamKey, urlParamValue) {
    /* if the param is a zipcode, replace any existing one. Otherwise, add it to existing params */    
    if (urlParamKey === "zipcode") {
      this.filterParams.set(urlParamKey, urlParamValue);      
    } else {
      this.filterParams.append(urlParamKey, urlParamValue);      
    }
    this.organizationObjectsList = [];
    this.organizationListArea.innerHTML = "";
    await this.getListOfOrganizations();
    await this.renderListOfOrganizations();
  }
}
