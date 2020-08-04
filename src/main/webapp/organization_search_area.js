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
  // This checks that requester User has valid credentials to edit/delete/view ALL organizations. (by checking userID)
  let forOrganizationsPage = false;
  const currentUser = new User();
  await currentUser.renderLoginStatus();
  let isMaintainer = currentUser.isMaintainer;

  if (document.getElementById('search-area')) {
    const mainSearchArea = new OrganizationSearchArea(document.getElementById('search-area'), isMaintainer, forOrganizationsPage);
  }
  if (document.getElementById('all-organizations')) {
    forOrganizationsPage = true;
    const organizationSearchArea = new OrganizationSearchArea(document.getElementById('all-organizations'), isMaintainer, forOrganizationsPage);
  }
});

class OrganizationSearchArea {
  constructor(searchAreaElement, isMaintainer, forOrganizationsPage) {
    this.searchArea = searchAreaElement;
    this.isMaintainer = isMaintainer;
    this.forOrganizationsPage = forOrganizationsPage;

    this.searchAreaObject = new SearchArea(this.searchArea,
      (objectsList, listArea) => { 
          return this.renderListOfOrganizations(objectsList, listArea) },
      (filterParams, objectsList, lastResultFound, loadMoreButton) => {
           return this.getListOfOrganizations(filterParams, objectsList, lastResultFound, loadMoreButton) });
    if (this.forOrganizationsPage && !this.isMaintainer) {
      this.searchAreaObject.filterParams.set("displayForUser", "true");
    }
    this.searchAreaObject.handleObjects();
  }

  renderListOfOrganizations(objectsList, listArea) {
    objectsList.forEach((organization) => {
      const newOrganization = new Organization(organization, this.isMaintainer, this.forOrganizationsPage);

      newOrganization.organizationElement.addEventListener('organization-selected', () => {
        const organizationPopupArea = document.getElementById("search-result-popup-area");
        organizationPopupArea.textContent = "";
        organizationPopupArea.appendChild(newOrganization.createOrganizationPopup());
        organizationPopupArea.classList.add("show-popup");
        organizationPopupArea.classList.remove("hide-popup");
      });

      newOrganization.closeButtonElement.addEventListener('organization-close', () => {
        //  Remove the popup from the DOM.
        document.getElementById("search-result-popup-area").classList.add("hide-popup");
        document.getElementById("search-result-popup-area").classList.remove("show-popup");
        newOrganization.popupElement.remove();
      });

      listArea.appendChild(newOrganization.getOrganization());
    });
    /* If the query has returned 0 organization objects, display No Results Found message */
    if ((objectsList.length === 0) && (listArea.innerHTML === '')) {
      const noResultsFoundMessage = document.createElement("div");
      noResultsFoundMessage.setAttribute("id", "no-results-found");
      noResultsFoundMessage.textContent = "No results found for current filters.";
      listArea.appendChild(noResultsFoundMessage);
    }
  }

  async getListOfOrganizations(filterParams, objectsList, lastResultFound, loadMoreButton) {
    const response = await fetch(`/list-organizations?${filterParams.toString()}`);
    objectsList = await response.json();
    const newCursor = await response.headers.get("Cursor");
    filterParams.set("cursor", newCursor);
    /* If < 5 results are returned, the end of the given query has been reached */
    lastResultFound = (objectsList.length < 5);
    if (lastResultFound) {
      loadMoreButton.classList.add("hide-load-button");
    }
    return objectsList;
  }
}
