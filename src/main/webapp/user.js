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
    this.loginData = await response.json();

    if (response.status !== 200) {
      throw new Error('Did not successfully authenticate user.');
      return;
    }

    if (this.loginData.isLoggedIn) {
      this.isMaintainer = this.loginData.isMaintainer;
      this.isModerator = this.loginData.moderatingOrgs.length >= 1;
    } else {
      const loginLink = document.getElementById("login-url");
      loginLink.textContent = "Log In";
      loginLink.setAttribute("href", this.loginData.url);
    }
  }

  createAddMaintainerPopup() {
    const addMaintainerModal = document.createElement("div");
    addMaintainerModal.setAttribute("id", "add-maintainer-popup-modal");

    const emailForm = document.createElement('form');
    emailForm.setAttribute("method", "POST");
    emailForm.setAttribute("id", "add-maintainer-email-form");

    const popupCloseButton = document.createElement("div");
    popupCloseButton.classList.add("add-maintainer-popup-close-button");
    popupCloseButton.textContent = "X";
    popupCloseButton.addEventListener("click", () => {
      addMaintainerModal.remove();
      document.getElementById("modal-popup-background").remove();
    });
    emailForm.appendChild(popupCloseButton);

    const emailInputLabel = document.createElement("div");
    emailInputLabel.setAttribute("id", "add-maintainer-label");
    emailInputLabel.textContent = "New Maintainer's Email:";
    emailForm.appendChild(emailInputLabel);

    const emailInputElement = document.createElement("input");
    emailInputElement.setAttribute("type", "text");
    emailInputElement.setAttribute("id", "maintainer-email-entry");
    emailInputElement.setAttribute("name", "userEmail");
    emailForm.appendChild(emailInputElement);

    const submitButton = document.createElement("input");
    submitButton.textContent = "Submit";
    submitButton.setAttribute("type", "submit");
    submitButton.setAttribute("class", "enter-button");
    emailForm.appendChild(submitButton);

    emailForm.setAttribute("action", "/add-maintainer");

    addMaintainerModal.appendChild(emailForm);
    return addMaintainerModal;
  }
}

document.addEventListener('DOMContentLoaded', async function() {
  const currentUser = new User();
  // Called to set log in/out URL when site loads.
  await currentUser.renderLoginStatus();
  if (currentUser.loginData.isLoggedIn && (document.getElementById("nav-bar"))) {
    rebuildNavBar(currentUser);
  }
});
