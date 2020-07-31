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
    const currentUser = new User();
    // Called to set log in/out URL when site loads.
    await currentUser.renderLoginStatus();
    await createOrganizationInChargeInput(document.getElementById("organization-in-charge-area"), currentUser);
    const optionArea = document.getElementById("hours-option-area");
    const timeOption = new TimeOption("Event hours", true, null, optionArea, false);
});

async function createOrganizationInChargeInput(listArea, currentUser) {
    // fetch organizations that current user is a moderator of or fetch all organizations
    // if it is a maintainer
    let organizations;
    if (currentUser.isModerator && (!currentUser.isMaintainer)) {
      const response = await fetch(`/list-organizations?displayForUser=true&cursor=all`);
      organizations = await response.json();
    } else if (currentUser.isMaintainer) {
      
      const response = await fetch(`/list-organizations?cursor=all`);
      organizations = await response.json();
    }

    const options = document.createElement("select");
    options.setAttribute("id", "organizations");
    options.setAttribute("name", "event-primary-organization-id");

    organizations.forEach((organization) => {
        const option = document.createElement("option");
        option.textContent = organization.name;
        option.setAttribute("value", organization.id);
        options.appendChild(option);
    });

    listArea.appendChild(options);
}
