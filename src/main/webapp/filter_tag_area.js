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

/* 
 * FilterTagArea holds the button to add new filters, the text field where the user enters
 * info for a new filter, and the filter tag itself after the user finishes adding it.
 * The text field for a new filter is the FilterEntry class, and the display of that new filter is
 * handled  by the FilterTag class.
 */

class FilterTagArea {
 
  constructor(searchArea) {
    this.parentSearchArea = searchArea;

    /* Active filter area holds the add filter button, the new filter text box, and any active filters */
    this.activeFilterArea = document.createElement("div");
    this.activeFilterArea.setAttribute("class", "filter-holder");

    this.filterEntry = new FilterEntry(this.activeFilterArea);
    this.filterEntry.filterEntryArea.addEventListener('onRemove', (e) => this.onRemoveCallback());

    /* This button does not move- when clicked it opens a filter entry area */
    this.addFilterButton = document.createElement("div");
    this.addFilterButton.setAttribute("class", "filter-tag-area");
    this.addFilterButton.setAttribute("id", "add-filter-button");
    this.addFilterButton.textContent = "Add Filter";
    this.addFilterButton.addEventListener('click', () => {
      /* If the user is on the second part of entering a filter, add filter is disabled */
      if (!this.filterEntry.hasFilterField()) {
        this.filterEntry.setupFilterEntry()
      }
    });
    this.activeFilterArea.appendChild(this.addFilterButton);

    this.parentSearchArea.searchArea.appendChild(this.activeFilterArea);
  }

  onRemoveCallback() {
    this.activeFilterArea.removeChild(this.filterEntry.filterEntryArea)
  }

  async addFilterTag(urlParamKey, urlParamValue) {
    let filterTag = new FilterTag(this, urlParamKey, urlParamValue);
    this.activeFilterArea.appendChild(filterTag.filterTagArea);
  }

  async removeFilterTag(urlParamKey, urlParamValue, filterTag, refreshObjects) {
    if (urlParamKey === "resourceCategories") {
      if (this.parentSearchArea.filterParams.getAll(urlParamKey).length === 1) {
        /* If only 1 filter param, delete the array */
        this.parentSearchArea.filterParams.delete(urlParamKey);
      } else {
        /* If not, just remove specified element */
        let filterArray = this.parentSearchArea.filterParams.getAll(urlParamKey);
        filterArray.splice(filterArray.indexOf(urlParamValue), 1);
        this.parentSearchArea.filterParams.set(urlParamKey, filterArray);
      }
    } else {
      this.parentSearchArea.filterParams.delete(urlParamKey);
    }
    this.activeFilterArea.removeChild(document.getElementById(urlParamKey));
    if (refreshObjects) {
      this.parentSearchArea.refreshObjectsList();
    }
  }
}
