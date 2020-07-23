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
    organizationSearchArea.handleOrganizations();
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
    this.mostRecentCursor = "none";
    this.lastResultFound = false;

    this.zipcodeFormArea = document.createElement("div");
    this.zipcodeFormArea.setAttribute("id", "zipcode-form-area");
    this.form = document.createElement("form");
    this.form.setAttribute("onsubmit", "return false");

    this.zipcodeFormLabel = document.createElement("label");
    this.zipcodeFormLabel.setAttribute("for", "zipcode-entry");
    this.zipcodeFormLabel.setAttribute("id", "zipcode-label");
    this.zipcodeFormLabel.textContent = "Enter Zipcode: ";
    this.form.appendChild(this.zipcodeFormLabel);

    this.zipcodeFormEntry = document.createElement("input");
    this.zipcodeFormEntry.setAttribute("type", "text");
    this.zipcodeFormEntry.setAttribute("id", "zipcode-entry");
    this.zipcodeFormEntry.setAttribute("pattern", "[0-9]{5}");
    this.zipcodeFormEntry.setAttribute("name", "zipcode");
    this.form.appendChild(this.zipcodeFormEntry);

    this.zipcodeSubmit = document.createElement("input");
    this.zipcodeSubmit.setAttribute("type", "submit");
    this.zipcodeSubmit.setAttribute("class", "enter-button");
    this.zipcodeSubmit.addEventListener('click', () => this.setUrlParamValue("zipcode", this.form.zipcode.value));
    this.form.appendChild(this.zipcodeSubmit);

    this.zipcodeFormArea.appendChild(this.form);
    this.organizationSearchArea.appendChild(this.zipcodeFormArea);

    this.filterInputArea = document.createElement("input");
    this.filterInputArea.setAttribute("list", "filter-datalist");
    this.filterInputArea.setAttribute("id", "filter-input-area");
    this.filterInputArea.setAttribute("placeholder", "Filter Results");
    this.filterInputArea.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        /* When the user hits enter in the filter input area, it is added as a param */
        this.setUrlParamValue("filterParam", this.filterInputArea.value);
        this.filterInputArea.value = "";
      }
    });

    this.filterDataList = document.createElement("datalist");
    this.filterDataList.setAttribute("id", "filter-datalist");
    this.filterOptions = ["Foods", "Clothing", "Shelter"];
    for (const value of this.filterOptions) {
      const option = document.createElement("option");
      option.value = value;
      this.filterDataList.appendChild(option);
    }

    this.filterInputArea.appendChild(this.filterDataList);
    this.organizationSearchArea.appendChild(this.filterInputArea);

    this.loadMoreButton = document.createElement("div");
    this.loadMoreButton.setAttribute("class", "load-more-button");
    this.loadMoreButton.textContent = "See More Results";
    this.loadMoreButton.addEventListener('click', () => this.handleOrganizations());

    this.activeFilterArea = document.createElement("div");
    this.activeFilterArea.setAttribute("class", "filter-holder");
    this.organizationSearchArea.appendChild(this.activeFilterArea);

    this.organizationListArea = document.createElement("div");
    this.organizationListArea.setAttribute("id", "organization-list");
    
    this.organizationPopupArea = document.createElement("div");
    this.organizationPopupArea.setAttribute("id", "organization-popup-area");
    this.organizationPopupArea.classList.add("hide-popup");

    this.organizationSearchArea.appendChild(this.organizationListArea);
    this.organizationSearchArea.appendChild(this.loadMoreButton);
    this.searchArea.appendChild(this.organizationSearchArea);
    this.searchArea.appendChild(this.organizationPopupArea);
  }

  async handleOrganizations() {
    if (!this.lastResultFound) {
      await this.getListOfOrganizations();
      this.renderListOfOrganizations();
    }
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
    if ((this.organizationObjectsList.length === 0) && (!this.lastResultFound)) {
      const noResultsFoundMessage = document.createElement("div");
      noResultsFoundMessage.setAttribute("id", "no-results-found");
      noResultsFoundMessage.textContent = "No results found for current filters.";
      this.organizationListArea.appendChild(noResultsFoundMessage);
    }
  }

  async getListOfOrganizations() {
    let response;
    if (this.filterParams) {
      response = await fetch(`/list-organizations?scrs=${this.mostRecentCursor}&${this.filterParams.toString()}`);
    } else {
      response = await fetch(`/list-organizations?scrs=${this.mostRecentCursor}`);
    }
    this.organizationObjectsList = await response.json();
    this.mostRecentCursor = await response.headers.get("Cursor");
    /* If < 5 results are returned, the end of the given query has been reached */
    this.lastResultFound = (this.organizationObjectsList.length < 5);
    if (this.lastResultFound) {
      this.loadMoreButton.classList.add("hide-load-button");
    }
  }

  async setUrlParamValue(urlParamKey, urlParamValue) {   
    /* New query value is not added if it is a duplicate or empty/null */
    if (this.filterParams.getAll("filterParam").includes(urlParamValue) ||
        this.filterParams.getAll("zipcode").includes(urlParamValue) ||
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
    this.addFilterTag(urlParamKey, urlParamValue);
    this.mostRecentCursor = "none";
    this.lastResultFound = false;
    this.loadMoreButton.classList.remove("hide-load-button");
    this.organizationObjectsList = [];
    this.organizationListArea.innerHTML = "";
    this.handleOrganizations();
  }

  addFilterTag(urlParamKey, urlParamValue) {
    let filterTagArea = document.createElement("div");
    filterTagArea.setAttribute("class", "filter-tag-area");
    /* ID is given to zipcode tag so it can be removed if new one is added */
    if (urlParamKey === "zipcode") {
      filterTagArea.setAttribute("id", "zipcodeTag");
    }

    let filterTagClose = document.createElement("div");
    filterTagClose.addEventListener('click', () => this.removeFilterTag(urlParamKey, urlParamValue, filterTagArea));
    filterTagClose.textContent = 'X';
    filterTagClose.setAttribute("class", "filter-tag-close");
    filterTagArea.appendChild(filterTagClose);

    let filterTagLabel = document.createElement("div");
    filterTagLabel.textContent = urlParamValue;
    filterTagLabel.setAttribute("class", "filter-tag-label");
    filterTagArea.appendChild(filterTagLabel);

    this.activeFilterArea.appendChild(filterTagArea);
  }

  async removeFilterTag(urlParamKey, urlParamValue, filterTag) {
    if (urlParamKey === "zipcode") {
      this.filterParams.delete("zipcode");
    } else {
      if (this.filterParams.getAll("filterParam").length === 1) {
        /* If only 1 filter param, delete the array */
        this.filterParams.delete("filterParam");
      } else {
        /* If not, just remove specified element */
        let filterArray = this.filterParams.getAll("filterParam");
        filterArray.splice(filterArray.indexOf(urlParamValue), 1);
        this.filterParams.set("filterParam", filterArray);
      }
    }
    this.activeFilterArea.removeChild(filterTag);
    this.mostRecentCursor = "none";
    this.lastResultFound = false;
    this.loadMoreButton.classList.remove("hide-load-button");
    this.organizationObjectsList = [];
    this.organizationListArea.innerHTML = "";
    this.handleOrganizations();
  }
}