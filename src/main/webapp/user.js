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

class User {
  User() {
    document.getElementById('login-url').addEventListener('click', this.renderLoginStatus);
  }

  async renderLoginStatus() {
    const response = await fetch('/authenticate');
    const loginData = await response.json();

    if (response.status !== 200) {
      throw new Error('Did not successfully authenticate user.');
      return;
    }

    if (loginData.isLoggedIn) {
      this.isMaintainer = loginData.isMaintainer;

      // TODO(): add isModerator check
      this.rebuildNavBar(/*isModerator*/ false);
    }  else {
      const loginLink = document.getElementById("login-url");
      loginLink.textContent = "Log In";
      loginLink.setAttribute("href", loginData.url);
    }
  }

  createAddMaintainerPopup() {
    const navBar = document.getElementById("nav-bar");

    const emailForm = document.createElement('form');
    emailForm.setAttribute("method", "POST");
    emailForm.setAttribute("id", "add-maintainer-email-form");
    
    const emailInputElement = document.createElement("input");
    emailInputElement.setAttribute("type", "text");
    emailInputElement.setAttribute("id", "maintainer-email-entry");
    emailInputElement.setAttribute("name", "userEmail");
    emailForm.appendChild(emailInputElement);

    const submitButton = document.createElement("input");
    submitButton.textContent = "Submit";
    submitButton.setAttribute("type", "submit");
    submitButton.setAttribute("class", "gray-button");
    emailForm.appendChild(submitButton);

    const popupCloseButton = document.createElement("div");
    popupCloseButton.classList.add("popup-close-button");
    popupCloseButton.textContent = "X";
    popupCloseButton.addEventListener("click", () => {
      emailForm.classList.add("hide-popup");
      emailForm.classList.remove("show-popup");
    });
    emailForm.appendChild(popupCloseButton);

    emailForm.setAttribute("action", "/add-maintainer");

    navBar.appendChild(emailForm);
  }

  rebuildNavBar(isModerator) {
    const navBar = document.getElementById("nav-bar");
    navBar.textContent = "";

    const helpNearMeLink = document.createElement("a");
    helpNearMeLink.setAttribute("href", "index.html");
    helpNearMeLink.textContent = "Help Near Me";
    navBar.appendChild(helpNearMeLink);

    const registrationLink = document.createElement("a");
    registrationLink.setAttribute("href", "registration.html");
    registrationLink.textContent = "Register Organization";
    navBar.appendChild(registrationLink);

    // TODO: CHANGE to this.isMaintainer
    if(!this.isMaintainer) {
      const organizationsLink = document.createElement("a");
      organizationsLink.setAttribute("href", "organizations.html");
      organizationsLink.textContent = "Organizations";
      navBar.appendChild(organizationsLink);

      this.createAddMaintainerPopup();
      document.getElementById("add-maintainer-email-form").classList.add("hide-popup");

      const addMaintainerLabel = document.createElement("a");
      addMaintainerLabel.textContent = "Add Maintainer";
      addMaintainerLabel.addEventListener("click", () => {
        const emailForm = document.getElementById("add-maintainer-email-form");
        emailForm.classList.add("show-popup");
        emailForm.classList.remove("hide-popup");
      });
      navBar.appendChild(addMaintainerLabel);


      // this.maintainerAddForm = this.addMaintainerAddForm();
      // navBar.appendChild(this.maintainerAddForm);
    } else if (isModerator) {
      const organizationsLink = document.createElement("a");
      organizationsLink.setAttribute("href", "organizations.html");
      organizationsLink.textContent = "My Organizations";
      navBar.appendChild(organizationsLink);
    }
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const currentUser = new User();
  // Called to set log in/out URL when site loads.
  currentUser.renderLoginStatus();
});
