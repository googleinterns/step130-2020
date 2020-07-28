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
    createOrganizationInChargeInput(document.getElementById("organization-in-charge-area"));
    const optionArea = document.getElementById("hours-option-area");
    const timeOption = new TimeOption("Event hours", true, null, optionArea, false);
});

function createOrganizationInChargeInput(listArea) {
    // TODO(): fetch organizations that current user is a moderator of or fetch all organizations
    // if it is a maintainer
    const organizations = [{
        "name": "Organization A",
        "id": 1
    },
    {
        "name": "Organization B",
        "id": 2
    }];

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