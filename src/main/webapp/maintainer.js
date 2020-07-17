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
  // TODO: Move this input form to be visible for only Maintainers.
  
  const emailForm = document.createElement('form');
  emailForm.setAttribute("method", "POST");
  
  const emailInputElement = document.createElement("input");
  emailInputElement.setAttribute("type", "text");
  emailInputElement.setAttribute("id", "userEmail");
  emailInputElement.setAttribute("name", "userEmail");
  emailForm.appendChild(emailInputElement);

  const submitButton = document.createElement("input");
  submitButton.textContent = "Submit";
  submitButton.setAttribute("type", "submit");
  emailForm.appendChild(submitButton);

  emailForm.setAttribute("action", "/add-maintainer");
  
  document.body.appendChild(emailForm);
});
