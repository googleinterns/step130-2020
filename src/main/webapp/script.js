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

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById("login-url").addEventListener('click', renderLoginStatus);
  // Called to set log in/out URL when site loads.
  renderLoginStatus();
});

async function renderLoginStatus() {
  const response = await fetch('/add-user');
  const loginData = await response.json();

  if (loginData.isLoggedIn) {
    document.getElementById("login-url").innerText = "Log Out";
    document.getElementById("login-url").href = loginData.url;
  } else {
    document.getElementById("login-url").innerText = "Log In";
    document.getElementById("login-url").href = loginData.url;
  }
}