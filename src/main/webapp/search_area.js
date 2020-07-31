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

class SearchArea {
  constructor(searchAreaElement, renderListOfObjects, getListOfObjects) {
    this.searchAreaContainer = searchAreaElement;
    this.renderListOfObjects = renderListOfObjects;
    this.getListOfObjects = getListOfObjects;
    this.searchArea = document.createElement("div");
    this.filterParams = new URLSearchParams();
    this.objectsList = [];

    /* The cursor is the "cursor" filter param, and the keyword "none" is used to start at the beginning of the query */
    this.filterParams.set("cursor", "none");
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
    this.searchArea.appendChild(this.zipcodeFormArea);

    this.filterTagArea = new FilterTagArea(this);
    this.filterTagArea.filterEntry.filterEntryArea.addEventListener('onParamEntry',
      (e) => this.setUrlParamValue(e.detail.urlParamKey, e.detail.urlParamValue), true);

    this.loadMoreButton = document.createElement("div");
    this.loadMoreButton.setAttribute("class", "load-more-button");
    this.loadMoreButton.textContent = "See More Results";
    this.loadMoreButton.addEventListener('click', () => this.handleObjects());

    this.listArea = document.createElement("div");
    this.popupArea = document.createElement("div");
    this.listArea.setAttribute("id", "search-result-list");
    this.popupArea.setAttribute("id", "search-result-popup-area");
    this.popupArea.classList.add("hide-popup");

    this.searchArea.appendChild(this.listArea);
    this.searchArea.appendChild(this.loadMoreButton);
    this.searchAreaContainer.appendChild(this.searchArea);
    this.searchAreaContainer.appendChild(this.popupArea);
  }

  refreshObjectsList() {
    this.filterParams.set("cursor", "none");
    this.lastResultFound = false;
    this.loadMoreButton.classList.remove("hide-load-button");
    this.objectsList = [];
    this.listArea.innerHTML = "";
    this.handleObjects();
  }

  async handleObjects() {
    if (!this.lastResultFound) {
      this.objectsList = await this.getListOfObjects(this.filterParams, this.objectsList, this.lastResultFound, this.loadMoreButton);
      this.renderListOfObjects(this.objectsList, this.listArea);
    }
  }

  async setUrlParamValue(urlParamKey, urlParamValue) {
    /* New query value is not added if it is a duplicate or empty/null */
    if (this.filterParams.getAll(urlParamKey).includes(urlParamValue) ||
      (urlParamValue === null) || (urlParamValue.trim() === "")) {
      return;
    }

    /* Only resource categories can have multiple active filters */
    if (urlParamKey === "resourceCategories") {	
      this.filterParams.append(urlParamKey, urlParamValue);
    } else {	
      if (this.filterParams.get(urlParamKey)) {
        /* If there is an active param for this key, remove its tag so both aren't displayed */
        this.filterTagArea.removeFilterTag(urlParamKey, this.filterParams.get(urlParamKey), document.getElementById(urlParamKey));
      }
      this.filterParams.set(urlParamKey, urlParamValue);
    }
    this.form.reset();
    this.filterTagArea.addFilterTag(urlParamKey, urlParamValue);
  }
}
