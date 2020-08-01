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
  // This checks that requester User has valid credentials to edit/delete/view ALL events. (by checking userID)
  let isMaintainer = true;
  const eventSearchArea = new EventSearchArea(document.getElementById('events-search-area'), isMaintainer);
});

class EventSearchArea {
  constructor(searchAreaElement, isMaintainer) {
    this.searchArea = searchAreaElement;
    this.isMaintainer = isMaintainer;
    this.showMyEvents = false;

    this.myEventsAndAddButtons = document.getElementById("my-events-and-add-buttons");
    this.myEventsButton = document.createElement("a");
    this.myEventsButton.setAttribute("id", "events-button");
    this.myEventsButton.textContent = "Show My Events";
    this.myEventsButton.addEventListener('click', () => this.toggleMyEventsFilter());
    this.myEventsAndAddButtons.appendChild(this.myEventsButton);
    this.addEventButton = document.createElement("a");
    this.addEventButton.textContent = "Register an Event";
    this.addEventButton.setAttribute("id", "events-button");
    this.addEventButton.setAttribute("href", "register_event.html");
    this.myEventsAndAddButtons.appendChild(this.addEventButton);

    this.searchAreaObject = new SearchArea(this.searchArea,
      (objectsList, listArea) => { 
          return this.renderListOfEvents(objectsList, listArea) },
      (filterParams, objectsList, lastResultFound, loadMoreButton) => { 
          return this.getListOfEvents(filterParams, objectsList, lastResultFound, loadMoreButton) });
    this.searchAreaObject.handleObjects();
  }

  renderListOfEvents(objectsList, listArea) {
    // TODO(): for each event send in the event object and the if the user that did the request is a moderator for that event or not
    objectsList.forEach((event) => {
        console.log(event);
      // TODO(): set up official event object
      const newEvent = new Event(event, this.isMaintainer);

      newEvent.eventElement.addEventListener('event-selected', () => {
        const eventPopupArea = document.getElementById("search-result-popup-area");
        eventPopupArea.textContent = "";
        eventPopupArea.appendChild(newEvent.createEventPopup());
        eventPopupArea.classList.add("show-popup");
        eventPopupArea.classList.remove("hide-popup");
      });

      newEvent.closeButtonElement.addEventListener('event-close', () => {
        //  Remove the popup from the DOM.
        document.getElementById("search-result-popup-area").classList.add("hide-popup");
        document.getElementById("search-result-popup-area").classList.remove("show-popup");
        newEvent.popupElement.remove();
      });

      listArea.appendChild(newEvent.getEvent());
    });
    /* If the query has returned 0 event objects, display No Results Found message */
    if ((objectsList.length === 0) && (listArea.innerHTML === '')) {
      const noResultsFoundMessage = document.createElement("div");
      noResultsFoundMessage.setAttribute("id", "no-results-found");
      noResultsFoundMessage.textContent = "No results found for current filters.";
      listArea.appendChild(noResultsFoundMessage);
    }
  }

  async getListOfEvents(filterParams, objectsList, lastResultFound, loadMoreButton) {
    const response = await fetch(`/list-events?${filterParams.toString()}`);
    objectsList = await response.json();
    /* If < 5 results are returned, the end of the given query has been reached */
    lastResultFound = (objectsList.length < 5);
    if (lastResultFound) {
      loadMoreButton.classList.add("hide-load-button");
    }
    
    return objectsList;
  }

  /* Click of my events button toggles events to show my events vs all events */
  async toggleMyEventsFilter() {
    this.showMyEvents = !this.showMyEvents;
    this.searchAreaObject.filterParams.set("displayForUser", this.showMyEvents);

    if (this.showMyEvents) {
      this.myEventsButton.classList.add("selected");
      this.myEventsButton.textContent = "Show All Events";
    } else {
      this.myEventsButton.classList.remove("selected");
      this.myEventsButton.textContent = "Show My Events";
    }
    await this.searchAreaObject.refreshObjectsList();
    this.searchAreaObject.loadMoreButton.classList.add("hide-load-button");
  }
}
