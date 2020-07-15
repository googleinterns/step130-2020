document.addEventListener("DOMContentLoaded", async function() {
  if(document.getElementById("registration-form-area")) {
  const mondayTimeOption = new TimeOption("Monday", true, null);
  const tuesdayTimeOption = new TimeOption("Tuesday", true, null);
  const wednesdayTimeOption = new TimeOption("Wednesday", true, null);
  const thursdayTimeOption = new TimeOption("Thursday", true, null);
  const fridayTimeOption = new TimeOption("Friday", true, null);
  const saturdayTimeOption = new TimeOption("Saturday", true, null);
  const sundayTimeOption = new TimeOption("Sunday", true, null);
  }
});

class TimeOption {
  constructor(day, forRegistration, organization) {
    this.day = day;
    this.forRegistration = forRegistration;
    this.organization = organization;
    this.optionArea = document.getElementById("hours-option-area");

    this.dayOptionArea = document.createElement("div");
    this.dayOptionArea.classList.add("day-option-area");
    this.timeInputArea = document.createElement("div");

    this.dayLabel = document.createElement("label");
    this.dayLabel.textContent = `${this.day}: `;
    this.dayOptionArea.appendChild(this.dayLabel);

    this.dayOpenLabel = document.createElement("label");
    this.dayOpenLabel.textContent = "Open";
    this.dayOptionArea.appendChild(this.dayOpenLabel);
    this.dayOpenInput = document.createElement("input");
    this.dayOpenInput.setAttribute("type", "radio");
    this.dayOpenInput.setAttribute("name",`${this.day}-isOpen`);
    this.dayOptionArea.appendChild(this.dayOpenInput);

    this.dayClosedLabel = document.createElement("label");
    this.dayClosedLabel.textContent = " Closed";
    this.dayOptionArea.appendChild(this.dayClosedLabel);
    this.dayClosedInput = document.createElement("input");
    this.dayClosedInput.setAttribute("type", "radio");
    this.dayClosedInput.setAttribute("name", `${this.day}-isOpen`);
    this.dayOptionArea.appendChild(this.dayClosedInput);

    this.radioElements = [this.dayOpenInput, this.dayClosedInput];

    if (this.forRegistration) {
      this.dayOpenInput.setAttribute("checked", "checked");
      this.timeInputArea.classList.add("show-time-area");
    } else if (this.organizationIsOpen == false) {
      this.dayClosedInput.setAttribute("checked", "checked");
      this.timeInputArea.classList.add("hide-time-area");
    } else {
      this.dayOpenInput.setAttribute("checked", "checked");
      this.timeInputArea.classList.add("show-time-area");
    }

    this.dayFromInput = document.createElement("input");
    this.dayFromInput.setAttribute("type", "time");
    this.dayFromInput.setAttribute("name", `${this.day}-from-time`);
    this.dayToInput = document.createElement("input");
    this.dayToInput.setAttribute("type", "time");
    this.dayToInput.setAttribute("name", `${this.day}-to-time`);

    this.timeInputArea.appendChild(this.dayFromInput);
    this.timeInputArea.appendChild(this.dayToInput);
    this.dayOptionArea.appendChild(this.timeInputArea);
    
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
    this.optionArea.appendChild(this.dayOptionArea);
  }
}

