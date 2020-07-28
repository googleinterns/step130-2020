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
  let isMaintainer = false;
  let forOrganizationsPage = false;
  let forEventsPage = false;
  if (document.getElementById('search-area')) {
    const mainSearchArea = new SearchArea(document.getElementById('search-area'), isMaintainer, forOrganizationsPage, forEventsPage);
    mainSearchArea.handleOrganizations();
  }
  if (document.getElementById('all-organizations')) {
    forOrganizationsPage = true;
    const organizationSearchArea = new SearchArea(document.getElementById('all-organizations'), isMaintainer, forOrganizationsPage, forEventsPage);
    organizationSearchArea.handleOrganizations();
  }
  if (document.getElementById('events')) {
    forEventsPage = true;
    const eventsSearchArea = new SearchArea(document.getElementById('events'), isMaintainer, forOrganizationsPage, forEventsPage);
  }
});

class SearchArea {
  constructor(searchAreaElement, isMaintainer, forOrganizationsPage, forEventsPage) {
    this.searchAreaContainer = searchAreaElement;
    this.searchArea = document.createElement("div");
    this.isMaintainer = isMaintainer;
    // TODO(): Add isModerator check after moderator PR is pulled in
    this.isModerator = true;
    this.forOrganizationsPage = forOrganizationsPage;
    this.forEventsPage = forEventsPage;
    this.filterParams = new URLSearchParams();
    this.objectsList = [];

    /* The cursor is the "cursor" filter param, and the keyword "none" is used to start at the beginning of the query */
    this.filterParams.set("cursor", "none");
    this.lastResultFound = false;

    if (forEventsPage && (this.isModerator || this.isMaintainer)) {
      this.myEventsAndAddButtons = document.createElement("div");
      this.myEventsAndAddButtons.setAttribute("id", "my-events-and-add-buttons");
      this.myEventsButton = document.createElement("div");
      this.myEventsButton.textContent = "My Events";
      this.myEventsButton.addEventListener('click', () => this.addMyEventsFilter());
      this.myEventsAndAddButtons.appendChild(this.myEventsButton);
      this.addEventButton = document.createElement("a");
      this.addEventButton.textContent = "Register an Event";
      this.addEventButton.setAttribute("href", "register_event.html");
      this.myEventsAndAddButtons.appendChild(this.addEventButton);
      this.searchArea.appendChild(this.myEventsAndAddButtons);
    }

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
    if (forEventsPage) {
      this.listArea.setAttribute("id", "event-list");
      this.popupArea.setAttribute("id", "event-popup-area");
      this.popupArea.classList.add("hide-popup");
    } else {
      this.listArea.setAttribute("id", "organization-list");
      this.popupArea.setAttribute("id", "organization-popup-area");
      this.popupArea.classList.add("hide-popup");
    }

    this.searchArea.appendChild(this.listArea);
    this.searchAreaContainer.appendChild(this.popupArea);
    this.searchArea.appendChild(this.loadMoreButton);
    this.searchAreaContainer.appendChild(this.searchArea);
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
      if (this.forEventsPage) {
        await this.getListOfEvents();
        this.renderListOfEvents();
      } else {
        await this.getListOfOrganizations();
        this.renderListOfOrganizations();
      }
    }
  }

  renderListOfOrganizations() {
    this.objectsList.forEach((organization) => {
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

      this.listArea.appendChild(newOrganization.getOrganization());
    });
    /* If the query has returned 0 organization objects, display No Results Found message */
    if ((this.objectsList.length === 0) && (this.listArea.innerHTML === '')) {
      const noResultsFoundMessage = document.createElement("div");
      noResultsFoundMessage.setAttribute("id", "no-results-found");
      noResultsFoundMessage.textContent = "No results found for current filters.";
      this.listArea.appendChild(noResultsFoundMessage);
    }
  }

  async getListOfOrganizations() {
    const response = await fetch(`/list-organizations?${this.filterParams.toString()}`);
    this.objectsList = await response.json();
    const newCursor = await response.headers.get("Cursor");
    this.filterParams.set("cursor", newCursor);
    /* If < 5 results are returned, the end of the given query has been reached */
    this.lastResultFound = (this.objectsList.length < 5);
    if (this.lastResultFound) {
      this.loadMoreButton.classList.add("hide-load-button");
    }
  }

  renderListOfEvents() {
    this.objectsList.forEach((event) => {
      const newEvent = new Event(event, this.isMaintainer, this.isModerator);

      newEvent.eventElement.addEventListener('event-selected', () => {
        const eventPopupArea = document.getElementById("event-popup-area");
        eventPopupArea.textContent = "";
        eventPopupArea.appendChild(newEvent.createEventPopup());
        eventPopupArea.classList.add("show-popup");
        eventPopupArea.classList.remove("hide-popup");
      });

      newEvent.closeButtonElement.addEventListener('event-close', () => {
        //  Remove the popup from the DOM.
        document.getElementById("event-popup-area").classList.add("hide-popup");
        document.getElementById("event-popup-area").classList.remove("show-popup");
        newEvent.popupElement.remove();
      });

      this.listArea.appendChild(newEvent.getEvent());
    });
    /* If the query has returned 0 event objects, display No Results Found message */
    if ((this.objectsList.length === 0) && (this.listArea.innerHTML === '')) {
      const noResultsFoundMessage = document.createElement("div");
      noResultsFoundMessage.setAttribute("id", "no-results-found");
      noResultsFoundMessage.textContent = "No results found for current filters.";
      this.listArea.appendChild(noResultsFoundMessage);
    }
  }

  async getListOfEvents() {
    // TODO(): Do correct fecth request
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
  }

  addMyEventsFilter() {
    // TODO(): Refine events by the ones fetching a list of events that are organized by 
    // organizations that they are a moderator of
  }
}
