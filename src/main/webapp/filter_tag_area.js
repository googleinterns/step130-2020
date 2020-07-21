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
      /* If the user is on the second part of entering a filter, add filter is disabled */
      if (!this.filterEntryArea.contains(this.filterParamInput)) {
        this.filterTypeInput.appendChild(this.filterDataList);
        this.filterEntryArea.appendChild(this.filterEntryClose);
        this.filterEntryArea.appendChild(this.filterTypeInput);
        this.activeFilterArea.appendChild(this.filterEntryArea);
      }
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
