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
 
document.addEventListener("DOMContentLoaded", async function() {
    let response = await fetch('/list-comments');
    let comments = await response.json();
    const commentArea = document.getElementById('comment-list');
    comments.forEach((comment) => {
      commentArea.appendChild(createComment(comment));
    });
  });
 
function createComment(comment) {
  const commentElement = document.createElement("div");
  commentElement.classList.add("comment");
 
  const commentNameElement = document.createElement('div');
  commentNameElement.classList.add("comment-name");
  commentNameElement.textContent = comment.name;
  const commentTextElement = document.createElement('div');
  commentTextElement.classList.add("comment-text");
  commentTextElement.textContent = comment.commentText;
 
  const deleteButtonElement = document.createElement('div');
  deleteButtonElement.classList.add("delete-button");
  deleteButtonElement.textContent = 'X';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(comment);
 
    // Remove the comment from the DOM.
    commentElement.remove();
  });
 
  commentElement.appendChild(commentNameElement);
  commentElement.appendChild(commentTextElement);
  commentElement.appendChild(deleteButtonElement);
  return commentElement;
}
 
/** Tells the server to delete the comment. */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  fetch('/delete-comment', {method: 'POST', body: params});
}
