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
 * FilterTag represents a completed filter that the user has added to the query, containing that 
 * filter's name and a button to delete it. A filter tag exists for each filter that is added.
 */
class FilterTag {
 
  constructor(filterTagArea, urlParamKey, urlParamValue) {
    this.parentFilterTagArea = filterTagArea;
  
    this.filterTagArea = document.createElement("div");
    this.filterTagArea.classList.add("filter-tag-area");
    this.filterTagArea.classList.add("filter-tag");
    /* ID is given to zipcode tag so it can be removed if new one is added */
    if (urlParamKey === "zipcode") {
      this.filterTagArea.setAttribute("id", "zipcodeTag");
    }

    this.filterTagClose = document.createElement("div");
    this.filterTagClose.textContent = 'X';
    this.filterTagClose.setAttribute("class", "filter-tag-close");
    this.filterTagClose.addEventListener('click', () => this.parentFilterTagArea.removeFilterTag(urlParamKey, urlParamValue, this));
    this.filterTagArea.appendChild(this.filterTagClose);
 
    this.filterTagLabel = document.createElement("div");
    this.filterTagLabel.setAttribute("class", "filter-tag-label");
    this.filterTagLabel.textContent = urlParamValue;
    this.filterTagArea.appendChild(this.filterTagLabel);
  }
}
