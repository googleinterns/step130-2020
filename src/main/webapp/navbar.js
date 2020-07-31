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

function rebuildNavBar(currentUser) {
  const navBar = document.getElementById("nav-bar");
  navBar.textContent = "";

  const siteTitle = document.createElement("div");
  siteTitle.setAttribute("id", "site-title");
  siteTitle.textContent = "Givr";
  navBar.appendChild(siteTitle);

  const navLinksArea = document.createElement("div");
  navLinksArea.setAttribute("id", "nav-links");

  const getHelpLink = document.createElement("a");
  getHelpLink.setAttribute("href", "index.html");
  getHelpLink.textContent = "Get Help";
  navLinksArea.appendChild(getHelpLink);

  const eventsLink = document.createElement("a");
  eventsLink.setAttribute("href", "events.html");
  eventsLink.textContent = "Events";
  navLinksArea.appendChild(eventsLink);

  const registerOrganizationLink = document.createElement("a");
  registerOrganizationLink.setAttribute("href", "register_organization.html");
  registerOrganizationLink.textContent = "Register Organization";
  navLinksArea.appendChild(registerOrganizationLink);

  if (currentUser.isMaintainer) {
    const organizationsLink = document.createElement("a");
    organizationsLink.setAttribute("href", "organizations.html");
    organizationsLink.textContent = "Organizations";
    navLinksArea.appendChild(organizationsLink);

    const addMaintainerPopup = currentUser.createAddMaintainerPopup();
    const addMaintainerLabel = document.createElement("a");
    addMaintainerLabel.textContent = "Add Maintainer";
    addMaintainerLabel.setAttribute("id", "add-maintainer-label");
    addMaintainerLabel.addEventListener("click", () => {
      const addMaintainerPopupBackground = document.createElement("div");
      addMaintainerPopupBackground.setAttribute("id", "modal-popup-background");
      addMaintainerPopupBackground.classList.add("add-maintainer-popup-modal-background");
      addMaintainerPopupBackground.appendChild(addMaintainerPopup);

      document.body.prepend(addMaintainerPopupBackground);

      addMaintainerPopup.classList.add("show-popup");
      addMaintainerPopup.classList.remove("hide-popup");
    });
    navLinksArea.appendChild(addMaintainerLabel);   
  } else if (currentUser.isModerator) {
    const organizationsLink = document.createElement("a");
    organizationsLink.setAttribute("href", "organizations.html");
    organizationsLink.textContent = "My Organizations";
    navLinksArea.appendChild(organizationsLink);
  }
  navBar.appendChild(navLinksArea);
}