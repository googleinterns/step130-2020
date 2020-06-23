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

document.addEventListener("DOMContentLoaded", function() {
  const slides = new Slides(document.getElementById("slide"));
});

class Slides {
  constructor(slideElement) {
    this.currSlide = 1;
    this.numImgs = 3;
    this.slide = slideElement;

    this.prev = document.createElement('a');
    this.prev.setAttribute("id", "prev");
    this.prev.textContent = "❮";
    this.slide.appendChild(this.prev);

    this.img = document.createElement("img");
    this.img.setAttribute("src", `images/img${this.currSlide}.jpg`)
    this.img.setAttribute("id", "slide-img");
    this.slide.appendChild(this.img);

    this.next = document.createElement('a');
    this.next.setAttribute("id", "next");
    this.next.textContent = "❯";
    this.slide.appendChild(this.next);

    this.slideNum = document.createElement("div");
    this.slideNum.textContent = `${this.currSlide} of ${this.numImgs}`;
    this.slideNum.setAttribute("id", "slide-number");
    this.slide.appendChild(this.slideNum);

    this.prev.onclick = () => {
      this.changeSlide(-1);
    }

    this.next.onclick = () => {
      this.changeSlide(1);
    }
  }

  changeSlide (direction) {
    this.currSlide += direction;
    if(this.currSlide < 1) {
      this.currSlide = this.numImgs;
    } else if(this.currSlide > this.numImgs) {
      this.currSlide = 1;
    }
    this.updateSlideContent();
  }

  updateSlideContent() {
    document.getElementById("slide-img").src = `images/img${this.currSlide}.jpg`;
    document.getElementById("slide-number").textContent = `${this.currSlide} of ${this.numImgs}`;
  }
}
