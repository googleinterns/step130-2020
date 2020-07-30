// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.event/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

class Event {
  constructor(event, isMaintainer = true, isModerator = true) {
    this.event = event;
    this.isMaintainer = isMaintainer;
    this.isModerator = isModerator;
    this.eventElement = document.createElement("div");
    this.closeButtonElement = document.createElement("div");
    this.popupElement = document.createElement("div");
    this.createEvent();
  }

  getEvent() {
    return this.eventElement;
  }

  createEvent() {
    this.eventElement.addEventListener('click', () => {
      let event = new CustomEvent('event-selected');
      this.eventElement.dispatchEvent(event);
    });

    this.closeButtonElement.addEventListener('click', () => {
      let event = new CustomEvent('event-close');
      this.closeButtonElement.dispatchEvent(event);
    });

    this.eventElement.classList.add("event");

    const eventNameElement = document.createElement('div');
    eventNameElement.classList.add("event-name");
    eventNameElement.textContent = this.event.name;

    const eventDateElement = document.createElement('div');
    eventDateElement.classList.add("event-date");
    eventDateElement.textContent = this.event.date;

    const eventTimeElement = document.createElement("div");
    eventTimeElement.classList.add("event-time");
    eventTimeElement.textContent = this.event.time;

    const eventDateTimeElement = document.createElement("div");
    eventDateTimeElement.classList.add("event-date-time");
    eventDateTimeElement.appendChild(eventDateElement);
    eventDateTimeElement.appendChild(eventTimeElement);

    const eventLocationElement = document.createElement("div");
    eventLocationElement.classList.add("event-location");
    eventLocationElement.textContent = `${this.event.streetAddress}, ${this.event.city}, ${this.event.state} ${this.event.zipcode}`;

    this.eventElement.appendChild(eventNameElement);
    this.eventElement.appendChild(eventDateTimeElement);
    this.eventElement.appendChild(eventLocationElement);
  }

  createCloseButton() {
    this.closeButtonElement.classList.add("popup-close-button");
    this.closeButtonElement.textContent = 'X';
  }

  createEventPopup() {

    this.popupElement = document.createElement("div");
    this.popupElement.classList.add("event-popup");

    const popupNameElement = document.createElement('div');
    popupNameElement.classList.add("event-popup-name");
    popupNameElement.textContent = this.event.name;

    const popupAddressElement = document.createElement('div');
    popupAddressElement.classList.add("event-popup-address");
    popupAddressElement.textContent = `${this.event.streetAddress}, ${this.event.city}, ${this.event.state} ${this.event.zipcode}`;
    
    const popupContactNameElement = document.createElement('div');
    popupContactNameElement.classList.add("event-popup-contact-name");
    popupContactNameElement.textContent = "Conact Name: " + this.event.contactName;

    const popupPhoneElement = document.createElement('div');
    popupPhoneElement.classList.add("event-popup-phone");
    popupPhoneElement.textContent = "Conact Phone: " + this.event.contactPhone;

    const popupEmailElement = document.createElement('div');
    popupEmailElement.classList.add("event-popup-email");
    popupEmailElement.textContent = "Contact Email: " + this.event.contactEmail;

    const popupDateElement = document.createElement('div');
    popupDateElement.classList.add("event-date");
    popupDateElement.textContent = this.event.date;

    const popupTimeElement = document.createElement("div");
    popupTimeElement.classList.add("event-time");
    popupTimeElement.textContent = this.event.time;

    const popupDateTimeElement = document.createElement("div");
    popupDateTimeElement.classList.add("event-popup-date-time");
    popupDateTimeElement.appendChild(popupDateElement);
    popupDateTimeElement.appendChild(popupTimeElement);

    const popupDetailsElement = document.createElement('div');
    popupDetailsElement.classList.add("event-popup-details");
    popupDetailsElement.textContent = "Details: " + this.event.details;

    const popupEditElement = document.createElement('button');
      popupEditElement.classList.add("enter-button");
      popupEditElement.textContent = "Edit";
      popupEditElement.addEventListener('click', () => {
        const editFormArea = document.getElementById("edit-form-area");
        editFormArea.appendChild(this.editEvent(this.event));
        editFormArea.classList.remove("hide-edit-modal");
        editFormArea.classList.add("show-edit-modal")
      });

    this.createCloseButton()

    this.popupElement.appendChild(this.closeButtonElement);
    this.popupElement.appendChild(popupNameElement);
    this.popupElement.appendChild(popupDateTimeElement);
    this.popupElement.appendChild(popupAddressElement);
    this.popupElement.appendChild(popupContactNameElement);
    this.popupElement.appendChild(popupPhoneElement);
    this.popupElement.appendChild(popupEmailElement);
    this.popupElement.appendChild(popupDetailsElement);
    if (this.isModerator || this.isMaintainer) {
      this.popupElement.appendChild(popupEditElement);
    }
    return this.popupElement;
  }

  editEvent(event) {
    // all entry fields will be prepopulated with the current values for user experience

    //use param list to pass in id to servlet
    const params = new URLSearchParams();
    params.append("id", event.id);

    const editFormAreaContent = document.createElement("div");
    editFormAreaContent.setAttribute("id", "edit-form-area-content");

    const closeButtonElement = document.createElement('div');
    closeButtonElement.classList.add("popup-close-button");
    closeButtonElement.textContent = 'X';
    closeButtonElement.addEventListener('click', () => {
      // Remove the popup from the DOM.
      const editFormArea = document.getElementById("edit-form-area");
      editFormArea.classList.remove("show-edit-modal");
      editFormArea.classList.add("hide-edit-modal")
      editFormAreaContent.remove();
    });
    editFormAreaContent.appendChild(closeButtonElement);

    // create edit form element
    const editForm = document.createElement("form");
    editForm.setAttribute("action", `/edit-event?${params.toString()}`);
    editForm.setAttribute("method", "POST");

    // label and entry area for event name
    const eventNameLabel = document.createElement("label");
    eventNameLabel.setAttribute("for", "name");
    eventNameLabel.setAttribute("id", "name-label");
    eventNameLabel.textContent = "Event Name: ";
    editForm.appendChild(eventNameLabel);

    const eventNameEntry = document.createElement("input");
    eventNameEntry.setAttribute("type", "text");
    eventNameEntry.setAttribute("id", "name");
    eventNameEntry.setAttribute("value", `${event.name}`);
    eventNameEntry.setAttribute("name", "event-name");
    eventNameEntry.classList.add("edit-entry");
    editForm.appendChild(eventNameEntry);

    // label and entry area for event address
    const eventAddressLabel = document.createElement("label");
    eventAddressLabel.setAttribute("for", "address");
    eventAddressLabel.setAttribute("id", "address-label");
    eventAddressLabel.textContent = "Street Address: ";
    editForm.appendChild(eventAddressLabel);

    const eventAddressEntry = document.createElement("input");
    eventAddressEntry.setAttribute("type", "text");
    eventAddressEntry.setAttribute("id", "address");
    eventAddressEntry.setAttribute("value", `${event.streetAddress}`);
    eventAddressEntry.setAttribute("name", "event-street-address");
    eventAddressEntry.classList.add("edit-entry");
    editForm.appendChild(eventAddressEntry);

    // label and entry area for event city
    const eventCityLabel = document.createElement("label");
    eventCityLabel.setAttribute("for", "city");
    eventCityLabel.setAttribute("id", "city-label");
    eventCityLabel.textContent = "City: ";
    editForm.appendChild(eventCityLabel);

    const eventCityEntry = document.createElement("input");
    eventCityEntry.setAttribute("type", "text");
    eventCityEntry.setAttribute("id", "city");
    eventCityEntry.setAttribute("value", `${event.city}`);
    eventCityEntry.setAttribute("name", "event-city");
    eventCityEntry.classList.add("edit-entry");
    editForm.appendChild(eventCityEntry);

    // label and entry area for event state
    const eventStateLabel = document.createElement("label");
    eventStateLabel.setAttribute("for", "state");
    eventStateLabel.setAttribute("id", "state-label");
    eventStateLabel.textContent = "State: ";
    editForm.appendChild(eventStateLabel);

    const eventStateEntry = document.createElement("input");
    eventStateEntry.setAttribute("type", "text");
    eventStateEntry.setAttribute("id", "state");
    eventStateEntry.setAttribute("value", `${event.state}`);
    eventStateEntry.setAttribute("name", "event-state");
    eventStateEntry.classList.add("edit-entry");
    editForm.appendChild(eventStateEntry);
   
    // label and entry area for event zipcode
    const eventZipcodeLabel = document.createElement("label");
    eventZipcodeLabel.setAttribute("for", "zipcode");
    eventZipcodeLabel.setAttribute("id", "zipcode-edit-label");
    eventZipcodeLabel.textContent = "Zipcode: ";
    editForm.appendChild(eventZipcodeLabel);

    const eventZipcodeEntry = document.createElement("input");
    eventZipcodeEntry.setAttribute("type", "text");
    eventZipcodeEntry.setAttribute("id", "zipcode");
    eventZipcodeEntry.setAttribute("pattern", "[0-9]{5}");
    eventZipcodeEntry.setAttribute("value", `${event.zipcode}`);
    eventZipcodeEntry.setAttribute("name", "event-zip-code");
    eventZipcodeEntry.classList.add("edit-entry");
    editForm.appendChild(eventZipcodeEntry);

    // label and entry area for event email
    const eventContactNameLabel = document.createElement("label");
    eventContactNameLabel.setAttribute("for", "contact-name");
    eventContactNameLabel.setAttribute("id", "contact-name-label");
    eventContactNameLabel.textContent = "Contact Name: ";
    editForm.appendChild(eventContactNameLabel);

    const eventContactNameEntry = document.createElement("input");
    eventContactNameEntry.setAttribute("type", "text");
    eventContactNameEntry.setAttribute("id", "contact-name");
    eventContactNameEntry.setAttribute("value", `${event.contactName}`);
    eventContactNameEntry.setAttribute("name", "event-contact-name");
    eventContactNameEntry.classList.add("edit-entry");
    editForm.appendChild(eventContactNameEntry);

    // label and entry area for event email
    const eventEmailLabel = document.createElement("label");
    eventEmailLabel.setAttribute("for", "email");
    eventEmailLabel.setAttribute("id", "email-label");
    eventEmailLabel.textContent = "Contact Email: ";
    editForm.appendChild(eventEmailLabel);

    const eventEmailEntry = document.createElement("input");
    eventEmailEntry.setAttribute("type", "text");
    eventEmailEntry.setAttribute("id", "email");
    eventEmailEntry.setAttribute("value", `${event.contactEmail}`);
    eventEmailEntry.setAttribute("name", "event-contact-email");
    eventEmailEntry.classList.add("edit-entry");
    editForm.appendChild(eventEmailEntry);

    // label and entry area for event phone
    const eventPhoneLabel = document.createElement("label");
    eventPhoneLabel.setAttribute("for", "phone");
    eventPhoneLabel.setAttribute("id", "phone-label");
    eventPhoneLabel.textContent = "Contact Phone: ";
    editForm.appendChild(eventPhoneLabel);

    const eventPhoneEntry = document.createElement("input");
    eventPhoneEntry.setAttribute("type", "text");
    eventPhoneEntry.setAttribute("id", "phone-num");
    eventPhoneEntry.setAttribute("pattern", "[0-9]{10}");
    eventPhoneEntry.setAttribute("value", `${event.contactPhone}`);
    eventPhoneEntry.setAttribute("name", "event-phone-num");
    eventPhoneEntry.classList.add("edit-entry");
    editForm.appendChild(eventPhoneEntry);

    //TODO() : Add event hour and date option change

    // label and entry area for event description
    const eventDetailsLabel = document.createElement("label");
    eventDetailsLabel.setAttribute("for", "details");
    eventDetailsLabel.setAttribute("id", "details-label");
    eventDetailsLabel.textContent = "Details: ";
    editForm.appendChild(eventDetailsLabel);

    const eventDetailsEntry = document.createElement("textarea");
    eventDetailsEntry.setAttribute("type", "text");
    eventDetailsEntry.setAttribute("id", "details");
    eventDetailsEntry.setAttribute("name", "event-details");
    eventDetailsEntry.classList.add("edit-entry");
    eventDetailsEntry.textContent = event.details;
    editForm.appendChild(eventDetailsEntry);

    const editFormSubmit = document.createElement("input");
    editFormSubmit.setAttribute("type", "submit");
    editFormSubmit.setAttribute("class", "enter-button");
    editForm.appendChild(editFormSubmit);

    editFormAreaContent.appendChild(editForm);

    return editFormAreaContent;
  }

  createOpenHoursText(eventDay) {
    // event.hoursOpen[index(1-7 for day of week)].propertyMap.
    // fromToPairs.value[index(how many set of hours for that day )].propertyMap.from/to
    const dayTimeArea = document.createElement("div");
    dayTimeArea.classList.add("day-time-area");

    const dayTimeText = document.createElement("p");
    dayTimeText.textContent = `${eventDay.propertyMap.day}: `;

    if (eventDay.propertyMap.isOpen) {
      let fromToString = null;
      const numPairs = eventDay.propertyMap.fromToPairs.value.length;

      // creates from to text in the form of hh:mm - hh:mm
      for (let i = 0; i < numPairs; i++) {
        
        // parses it from 24 hour format to 12 hour format
        let from = this.parseTime(eventDay.propertyMap.fromToPairs.value[i].propertyMap.from);
        let to = this.parseTime(eventDay.propertyMap.fromToPairs.value[i].propertyMap.to);

        // adds a comma if it is not the last pair in the list
        fromToString = `${from} - ${to}`;
        if (numPairs - 1 != i) {
          fromToString += `, \n`;
        }
        else {
          fromToString += `\n`;
        }
        dayTimeText.textContent += fromToString;
      }
    }
    else {
      dayTimeText.textContent += `Closed`;
    }

    dayTimeArea.appendChild(dayTimeText);
    return dayTimeArea;
  }

  parseTime(time) {
    const hoursAndMinutes = time.split(":");
    let AMOrPM = null;
    let hours = parseInt(hoursAndMinutes[0]);
    let minutes = parseInt(hoursAndMinutes[1]);

    if (hours > 12) {
      AMOrPM = "PM";
      hours -= 12;
    } else if (hours == 12) {
        AMOrPM ="PM";
    } else if (hours == 0) {
        AMOrPM = "AM";
        hours = 12;
    } else {
      AMOrPM = "AM";
    }
    
    let hoursString = hours.toString();
    let minutesString = minutes.toString();
  
    if (minutes < 10) {
      minutesString = `0${minutes.toString()}`;
    }
    return `${hoursString}:${minutesString} ${AMOrPM}`;
  }
}