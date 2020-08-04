// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

document.addEventListener("DOMContentLoaded", function() {
    const footer = createFooter(document.getElementById("footer"));
});

function createFooter(footerArea) {
    const githubLink = document.createElement("a");
    githubLink.setAttribute("href", "https://github.com/googleinterns/step130-2020");

    const githubImg = document.createElement("img");
    githubImg.setAttribute("src", "images/github.png");
    githubImg.setAttribute("id", "github-img");
    githubLink.appendChild(githubImg);

    footerArea.appendChild(githubLink);

    const teammate1 = document.createElement("div");
    teammate1.textContent = "Jenny Baik";
    teammate1.classList.add("teammate-name");
    footerArea.appendChild(teammate1);

    const teammate2 = document.createElement("div");
    teammate2.textContent = "Andrew Masek";
    teammate2.classList.add("teammate-name");
    footerArea.appendChild(teammate2);

    const teammate3 = document.createElement("div");
    teammate3.textContent = "Sarah Addo";
    teammate3.classList.add("teammate-name");
    footerArea.appendChild(teammate3);
}
