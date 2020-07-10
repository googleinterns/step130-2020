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

async function getListOfOrganizations(urlSearchParams) {
  let response;
  if (urlSearchParams) {
    response = await fetch(`/list-organizations?${urlSearchParams.toString()}`);
  } else {
    response = await fetch(`/list-organizations`);
  }
  const organizations = await response.json();

  return organizations;
}

document.addEventListener("DOMContentLoaded", async function() {
  const organizations = await getListOfOrganizations();
  // TODO: Get Maintainer status by checking if requester User is a Maintainer. 
  // This checks that requester User has valid credentials to edit/delete/view ALL organizations. (by checking userID)
  let isMaintainer = false;

  // TODO: Create separate JS file for listing organizations, instead of using SearchArea and having these checks below
  if (document.getElementById('search-area')) {
    const mainSearchArea = new SearchArea(document.getElementById('search-area'), organizations, isMaintainer);
  }
  isMaintainer = true;
  if (document.getElementById('all-organizations')) {
    const organizationSearchArea = new SearchArea(document.getElementById('all-organizations'), organizations, isMaintainer);
  }
});

class SearchArea {
  constructor(searchAreaElement, organizations, isMaintainer) {
    this.searchArea = searchAreaElement;
    this.organizationSearchArea = document.createElement("div");
    this.organizationsObjectsList = organizations;
    this.isMaintainer = isMaintainer;
    this.filterParams = new URLSearchParams();

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
    this.zipcodeSubmit.addEventListener('click', () => this.handleZipcodeSubmission(this.form));
    this.form.appendChild(this.zipcodeSubmit);

    this.zipcodeFormArea.appendChild(this.form);
    this.organizationSearchArea.appendChild(this.zipcodeFormArea);

    this.filterInputArea = document.createElement("input");
    this.filterInputArea.setAttribute("list", "filter-datalist");
    this.filterInputArea.setAttribute("id", "filter-input-area");
    this.filterInputArea.setAttribute("placeholder", "Filter Results");
    this.filterInputArea.addEventListener('keypress', function (e) {
      if (e.key === 'Enter') {
        this.handleFilterSubmission(this.filterInputArea.value);
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

    this.organizationListDiv = document.createElement("div");
    this.organizationListDiv.setAttribute("id", "organization-list");
    
    this.organizationPopupArea = document.createElement("div");
    this.organizationPopupArea.setAttribute("id", "organization-popup-area");
    this.organizationPopupArea.classList.add("hide-popup");

    this.renderListOfOrganizations();

    this.organizationSearchArea.appendChild(this.organizationListDiv);
    this.searchArea.appendChild(this.organizationSearchArea);
    this.searchArea.appendChild(this.organizationPopupArea);
  }

  async renderListOfOrganizations() {
    this.organizationsObjectsList.forEach((organization) => {
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

      this.organizationListDiv.appendChild(newOrganization.getOrganization());
    });
  }

  async handleZipcodeSubmission(zipcodeForm) {
    this.filterParams.set("zipcode", zipcodeForm.zipcode.value);
    this.organizationsObjectsList = [];
    this.organizationListDiv.innerHTML = "";
    this.organizationsObjectsList = await getListOfOrganizations(this.filterParams);
    this.renderListOfOrganizations();
  }

  async handleFilterSubmission(filterParam) {
    /* There can be more than one filterParam if the user clicks another without refreshing/clearing */
    this.filterParams.append("filterParam", filterParam);
    this.organizationsObjectsList = [];
    this.organizationListDiv.innerHTML = "";
    this.organizationsObjectsList = await getListOfOrganizations(this.filterParams);
    this.renderListOfOrganizations();
  }
}
