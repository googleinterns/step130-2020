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
  // Looks for the hours-option-area in dom and will append the option to enter times 
  // for when the organization is open on every day of the week
  if (document.getElementById("registration-form-area")) {
    const optionArea = document.getElementById("hours-option-area");
    const mondayTimeOption = new TimeOption("Monday", /*forRegistration*/ true, /*organizationDay*/ null, optionArea, /*showOpenClosedOptions*/ true);
    const tuesdayTimeOption = new TimeOption("Tuesday", /*forRegistration*/ true, /*organizationDay*/ null, optionArea, /*showOpenClosedOptions*/ true);
    const wednesdayTimeOption = new TimeOption("Wednesday", /*forRegistration*/ true, /*organizationDay*/ null, optionArea, /*showOpenClosedOptions*/ true);
    const thursdayTimeOption = new TimeOption("Thursday", /*forRegistration*/ true, /*organizationDay*/ null, optionArea, /*showOpenClosedOptions*/ true);
    const fridayTimeOption = new TimeOption("Friday", /*forRegistration*/ true, /*organizationDay*/ null, optionArea, /*showOpenClosedOptions*/ true);
    const saturdayTimeOption = new TimeOption("Saturday", /*forRegistration*/ true, /*organizationDay*/ null, optionArea, /*showOpenClosedOptions*/ true);
    const sundayTimeOption = new TimeOption("Sunday", /*forRegistration*/ true, /*organizationDay*/ null, optionArea, /*showOpenClosedOptions*/ true);
  }
});

class TimeOption {
  // organization.hoursOpen[index(1-7 for day of week)].propertyMap.
  // fromToPairs.value[index(how many set of hours for that day )].propertyMap.from/to
  constructor(day, forRegistration, organizationDay, optionArea, showOpenClosedOptions) {
    this.day = day;
    this.forRegistration = forRegistration;
    this.organizationDay = organizationDay.propertyMap;
    this.optionArea = optionArea;
    this.showOpenClosedOptions = showOpenClosedOptions

    this.dayOptionArea = document.createElement("div");
    this.dayOptionArea.classList.add("day-option-area");
    this.timeInputArea = document.createElement("div");

    this.dayLabel = document.createElement("label");
    this.dayLabel.textContent = `${this.day}:`;
    this.dayLabel.classList.add("day-label");
    this.dayOptionArea.appendChild(this.dayLabel);

    if (this.showOpenClosedOptions) {
      this.dayOpenLabel = document.createElement("label");
      this.dayOpenLabel.textContent = "Open";
      this.dayOpenLabel.classList.add("day-open-label");
      this.dayOptionArea.appendChild(this.dayOpenLabel);
      this.dayOpenInput = document.createElement("input");
      this.dayOpenInput.setAttribute("type", "radio");
      this.dayOpenInput.setAttribute("name", `${this.day}-isOpen`);
      this.dayOpenInput.setAttribute("value", "open");
      this.dayOptionArea.appendChild(this.dayOpenInput);

      this.dayClosedLabel = document.createElement("label");
      this.dayClosedLabel.textContent = "Closed";
      this.dayClosedLabel.classList.add("day-closed-label");
      this.dayOptionArea.appendChild(this.dayClosedLabel);
      this.dayClosedInput = document.createElement("input");
      this.dayClosedInput.setAttribute("type", "radio");
      this.dayClosedInput.setAttribute("name", `${this.day}-isOpen`);
      this.dayClosedInput.setAttribute("value", "closed");
      this.dayOptionArea.appendChild(this.dayClosedInput);

      this.radioElements = [this.dayOpenInput, this.dayClosedInput];
      if (this.forRegistration) {
        this.dayOpenInput.setAttribute("checked", "checked");
        this.timeInputArea.classList.add("show-time-area");
      } else if (!this.organizationDay.isOpen) {
        this.dayClosedInput.setAttribute("checked", "checked");
        this.timeInputArea.classList.add("hide-time-area");
      } else {
        this.dayOpenInput.setAttribute("checked", "checked");
        this.timeInputArea.classList.add("show-time-area");
      }
      this.radioElements.forEach((elem) => {
        elem.addEventListener("change", () => {
          if (this.dayOpenInput.checked) {
            this.timeInputArea.classList.add("show-time-area");
            this.timeInputArea.classList.remove("hide-time-area");
          } else {
            this.timeInputArea.classList.add("hide-time-area");
            this.timeInputArea.classList.remove("show-time-area");
          }
        });
      });
    }

    this.dayFromInput = document.createElement("input");
    this.dayFromInput.setAttribute("type", "time");
    this.dayFromInput.setAttribute("name", `${this.day}-from-times`);
    this.dayToInput = document.createElement("input");
    this.dayToInput.setAttribute("type", "time");
    this.dayToInput.setAttribute("name", `${this.day}-to-times`);

    // for edit page get the number of pairs of time input options to preset the values
    let numPairs = 0;
    if (!forRegistration && !this.showOpenClosedOptions) {
      numPairs = this.organizationDay.fromToPairs.value.length;

      // set initial from to pair, if organization is open there is at least one pair
      this.dayFromInput.setAttribute("value", this.organizationDay.fromToPairs.value[0].propertyMap.from);
      this.dayToInput.setAttribute("value", this.organizationDay.fromToPairs.value[0].propertyMap.to);
    } else if (!forRegistration && this.organizationDay.isOpen) {
      numPairs = this.organizationDay.fromToPairs.value.length;

      // set initial from to pair, if organization is open there is at least one pair
      this.dayFromInput.setAttribute("value", this.organizationDay.fromToPairs.value[0].propertyMap.from);
      this.dayToInput.setAttribute("value", this.organizationDay.fromToPairs.value[0].propertyMap.to);
    }

    this.addMoreInput = document.createElement("a");
    this.addMoreInput.textContent = "+";
    this.addMoreInput.classList.add("add-more-time-button");
    this.addMoreInput.onclick = () => {
      this.addTimeInputOption(null, null);
    }

    this.timeInputArea.appendChild(this.dayFromInput);
    this.timeInputArea.appendChild(this.dayToInput);
    this.timeInputArea.appendChild(this.addMoreInput);

    // if there are multiple pairs of previously inputted times then adds and sets those values
    for (let i = 1; i < numPairs; i++) {
      this.addTimeInputOption(this.organizationDay.fromToPairs.value[i].propertyMap.from,
        this.organizationDay.fromToPairs.value[i].propertyMap.to);
    }

    this.dayOptionArea.appendChild(this.timeInputArea);
    this.optionArea.appendChild(this.dayOptionArea);
  }

  addTimeInputOption(fromTime, toTime) {
    const newTimeInputArea = document.createElement("div");
    const dayFromInput = document.createElement("input");
    dayFromInput.setAttribute("type", "time");
    dayFromInput.setAttribute("name", `${this.day}-from-times`);
    const dayToInput = document.createElement("input");
    dayToInput.setAttribute("type", "time");
    dayToInput.setAttribute("name", `${this.day}-to-times`);
    if (fromTime != null && toTime != null) {
      dayFromInput.setAttribute("value", fromTime);
      dayToInput.setAttribute("value", toTime);
    }

    const addMoreInput = document.createElement("a");
    addMoreInput.classList.add("add-more-time-button");
    addMoreInput.textContent = "+";
    addMoreInput.onclick = () => {
      this.addTimeInputOption();
    }

    const removeInput = document.createElement("a");
    removeInput.textContent = "-";
    removeInput.classList.add("remove-more-time-button");
    removeInput.onclick = () => {
      newTimeInputArea.remove();
    }

    newTimeInputArea.appendChild(dayFromInput);
    newTimeInputArea.appendChild(dayToInput);
    newTimeInputArea.appendChild(addMoreInput);
    newTimeInputArea.appendChild(removeInput);
    this.timeInputArea.appendChild(newTimeInputArea);
  }
}
