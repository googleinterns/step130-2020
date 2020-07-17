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
  let forOrganizationsPage = false;

  // TODO: Create separate JS file for listing organizations, instead of using SearchArea and having these checks below
  if (document.getElementById('search-area')) {
    const mainSearchArea = new SearchArea(document.getElementById('search-area'), isMaintainer, forOrganizationsPage);
    await mainSearchArea.getListOfOrganizations();
    mainSearchArea.renderListOfOrganizations()
  }
  isMaintainer = true;
  if (document.getElementById('all-organizations')) {
    forOrganizationsPage = true;
    const organizationSearchArea = new SearchArea(document.getElementById('all-organizations'), isMaintainer, forOrganizationsPage);
    await organizationSearchArea.getListOfOrganizations();
    organizationSearchArea.renderListOfOrganizations();
  }
});

class SearchArea {
  constructor(searchAreaElement, isMaintainer, forOrganizationsPage) {
    this.searchArea = searchAreaElement;
    this.organizationSearchArea = document.createElement("div");
    this.isMaintainer = isMaintainer;
    this.forOrganizationsPage = forOrganizationsPage;
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

    this.filterTagArea = new FilterTagArea(this);

    this.organizationListArea = document.createElement("div");
    this.organizationListArea.setAttribute("id", "organization-list");
    
    this.organizationPopupArea = document.createElement("div");
    this.organizationPopupArea.setAttribute("id", "organization-popup-area");
    this.organizationPopupArea.classList.add("hide-popup");

    this.organizationSearchArea.appendChild(this.organizationListArea);
    this.searchArea.appendChild(this.organizationSearchArea);
    this.searchArea.appendChild(this.organizationPopupArea);
  }

  renderListOfOrganizations() {
    this.organizationObjectsList.forEach((organization) => {
      const newOrganization = new Organization(organization, this.isMaintainer, this.forOrganizationsPage);

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
    if (this.organizationObjectsList.length === 0) {
      const noResultsFoundMessage = document.createElement("div");
      noResultsFoundMessage.setAttribute("id", "no-results-found");
      noResultsFoundMessage.textContent = "No results found for current filters.";
      this.organizationListArea.appendChild(noResultsFoundMessage);
    }
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
    /* New query value is not added if it is a duplicate or empty/null */
    if (this.filterParams.getAll(urlParamKey).includes(urlParamValue) ||
        (urlParamValue === null) || (urlParamValue.trim() === "")) {
      return;
    }
    
    /* if the param is a zipcode, remove tag of any existing one & set new one*/ 
    if (urlParamKey === "zipcode") {
      if (this.filterParams.get("zipcode")) {
        /* If there is a zipcode being displayed, remove its tag so both aren't displayed */
        this.removeFilterTag("zipcode", this.filterParams.get("zipcode"), document.getElementById("zipcodeTag"));
      }
      this.filterParams.set(urlParamKey, urlParamValue);      
    } else {
      this.filterParams.append(urlParamKey, urlParamValue);      
    }
    this.form.reset();
    this.filterTagArea.addFilterTag(urlParamKey, urlParamValue);
    this.organizationObjectsList = [];
    this.organizationListArea.innerHTML = "";
    await this.getListOfOrganizations();
    this.renderListOfOrganizations();
  }
}

class FilterTagArea {

  constructor(searchArea) {
    this.parentSearchArea = searchArea;

    /* Active filter area holds the add filter button, the new filter text box, and any active filters */
    this.activeFilterArea = document.createElement("div");
    this.activeFilterArea.setAttribute("class", "filter-holder");

    /* filterEntryArea is displayed when the user clicks add filter. It first asks for a type of filter, 
       then asks the user to enter the keyword for that filter type */
    this.filterEntryArea = document.createElement("div");
    this.filterEntryArea.setAttribute("class", "filter-tag-area");
    this.filterEntryArea.setAttribute("id", "filter-entry");

    this.filterEntryClose = document.createElement("div");
    this.filterEntryClose.textContent = 'X';
    this.filterEntryClose.setAttribute("class", "filter-tag-close");
    this.filterEntryClose.addEventListener('click', () => {
      this.filterEntryArea.textContent = "";
      this.activeFilterArea.removeChild(this.filterEntryArea)
    });

    /* The user selects the type of property they want to filter by in filterTypeInput*/
    this.filterTypeInput = document.createElement("input");
    this.filterTypeInput.setAttribute("list", "filter-datalist");
    this.filterTypeInput.setAttribute("class", "filter-input-area");
    this.filterTypeInput.setAttribute("placeholder", "Filter by:");
    this.filterTypeInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        this.filterParamLabel.textContent = `${this.filterTypeInput.value}:`;
        this.filterTypeInput.removeChild(this.filterDataList);
        this.filterEntryArea.removeChild(this.filterTypeInput);
        this.filterEntryArea.appendChild(this.filterParamInput);
        this.filterEntryArea.appendChild(this.filterParamLabel);
      }
    });

    this.filterDataList = document.createElement("datalist");
    this.filterDataList.setAttribute("id", "filter-datalist");
    this.optionMap = new Map();
    this.optionMap.set("Organization Name", "orgNames");
    this.optionMap.set("Address", "orgStreetAddresses");
    this.optionMap.set("Available Resources", "resourceCategories");
    for (const optionKey of this.optionMap.keys()) {
      const option = document.createElement("option");
      option.value = optionKey;
      this.filterDataList.appendChild(option);
    }

    /* After the type has been chosen, the actual filter param is entered here */
    this.filterParamInput = document.createElement("input");
    this.filterParamInput.setAttribute("class", "filter-input-area");
    this.filterParamLabel = document.createElement("div");
    this.filterParamLabel.setAttribute("class", "filter-tag-label");
    this.filterParamInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        /* If the user enters an unsupported type, it is ignored on the servlet side */
        this.parentSearchArea.setUrlParamValue(this.optionMap.get(this.filterTypeInput.value), this.filterParamInput.value);
        this.filterParamInput.value = "";
        this.filterTypeInput.value = "";
        this.filterEntryArea.removeChild(this.filterParamInput);
        this.filterEntryArea.removeChild(this.filterParamLabel);
        this.activeFilterArea.removeChild(this.filterEntryArea);
      }
    });

    /* This button does not move- when clicked it opens a filter entry area */
    this.addFilterButton = document.createElement("div");
    this.addFilterButton.setAttribute("class", "filter-tag-area");
    this.addFilterButton.setAttribute("id", "add-filter-button");
    this.addFilterButton.textContent = "+ Add Filter";
    this.addFilterButton.addEventListener('click', () => {
      this.filterTypeInput.appendChild(this.filterDataList);
      this.filterEntryArea.appendChild(this.filterEntryClose);
      this.filterEntryArea.appendChild(this.filterTypeInput);
      this.activeFilterArea.appendChild(this.filterEntryArea);
    });
    this.activeFilterArea.appendChild(this.addFilterButton);

    this.parentSearchArea.organizationSearchArea.appendChild(this.activeFilterArea);
  }

  addFilterTag(urlParamKey, urlParamValue) {
    let filterTagArea = document.createElement("div");
    filterTagArea.setAttribute("class", "filter-tag-area");
    /* ID is given to zipcode tag so it can be removed if new one is added */
    if (urlParamKey === "zipcode") {
      filterTagArea.setAttribute("id", "zipcodeTag");
    }

    let filterTagLabel = document.createElement("div");
    filterTagLabel.textContent = urlParamValue;
    filterTagLabel.setAttribute("class", "filter-tag-label");
    filterTagArea.appendChild(filterTagLabel);

    let filterTagClose = document.createElement("div");
    filterTagClose.addEventListener('click', () => this.removeFilterTag(urlParamKey, urlParamValue, filterTagArea));
    filterTagClose.textContent = 'X';
    filterTagClose.setAttribute("class", "filter-tag-close");
    filterTagArea.appendChild(filterTagClose);

    this.activeFilterArea.appendChild(filterTagArea);
  }

  async removeFilterTag(urlParamKey, urlParamValue, filterTag) {
    if (urlParamKey === "zipcode") {
      this.parentSearchArea.filterParams.delete("zipcode");
    } else {
      if (this.parentSearchArea.filterParams.getAll(urlParamKey).length === 1) {
        /* If only 1 filter param, delete the array */
        this.parentSearchArea.filterParams.delete(urlParamKey);
      } else {
        /* If not, just remove specified element */
        let filterArray = this.parentSearchArea.filterParams.getAll(urlParamKey);
        filterArray.splice(filterArray.indexOf(urlParamValue), 1);
        this.parentSearchArea.filterParams.set(urlParamKey, filterArray);
      }
    }
    this.activeFilterArea.removeChild(filterTag);
    this.parentSearchArea.organizationObjectsList = [];
    this.parentSearchArea.organizationListArea.innerHTML = "";
    await this.parentSearchArea.getListOfOrganizations();
    this.parentSearchArea.renderListOfOrganizations();
  }
}