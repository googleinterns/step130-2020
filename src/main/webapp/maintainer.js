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
  // TODO: Add UI for adding Maintainer as a Maintainer. Create input box to submit on click and call /add-maintainer.
});

async function getMaintainerStatus(email) {
  const response = await fetch(`/add-maintainer?userEmail=${email}`);

  if (response.status !== 200) {
    throw new Error('Could not successfully add new maintainer.');
    return;
  }
}
