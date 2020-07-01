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

const COMMENT_SIZE_LIMIT = 100;
const facts =
  ["My ex podmate stole my go link", "I was the shortest person in my junior high", "I know 3 languages", "C++ is my favorite programming language"];

function getRandomFact() {
  const fact = facts[Math.floor(Math.random() * facts.length)];

  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

const createCommentElement = commentPayload => {
  const template = document.getElementById("comment-template");
  
  const clonedComment = template.cloneNode(true);
  clonedComment.classList.remove("hidden-element");

  const commentChildren = clonedComment.childNodes;

  const user = commentChildren[1];
  const commentWrapper = commentChildren[3];
  const bottomWrapper = commentChildren[5];

  const commentWrapperChildren = commentWrapper.childNodes;
  const bottomWrapperChildren = bottomWrapper.childNodes;

  const comment = commentWrapperChildren[1];
  const fullComment = commentWrapperChildren[3];

  const more = bottomWrapperChildren[1];
  const trash = bottomWrapperChildren[3];
  const like = bottomWrapperChildren[5];
  const numOfLikes = bottomWrapperChildren[7];

  const moreIcon = more.childNodes[0];
  const trashIcon = trash.childNodes[0];
  const likeIcon = like.childNodes[0];
  

  const commentText = commentPayload.comment;
  const id = String(commentPayload.id);

  if (commentPayload.size <= COMMENT_SIZE_LIMIT) {
    comment.innerHTML = commentText;
  } else {
    const shortenedComment = commentText.slice(0, COMMENT_SIZE_LIMIT);

    more.classList.remove("hidden-element");

    fullComment.innerHTML = commentText;

    comment.innerHTML = shortenedComment.concat("...");
    comment.id = id.concat("-visible");

    fullComment.id = id.concat("-hidden");

    more.id = id.concat("-btn");
    more.onclick = () => showFullText(id);
  }

  user.innerHTML = "@ ".concat(commentPayload.user);

  trash.onclick = () => removeComment(id);
  
  const likes = parseInt(commentPayload.likes);

  numOfLikes.innerHTML = "+".concat(String(likes));
  numOfLikes.id = id.concat("-likes");

  like.onclick = () => likeComment(id);

  clonedComment.id = id;
  return clonedComment;
}

function showFullText(id) {
  const hiddenId = id.concat("-hidden");
  const visibleId = id.concat("-visible");
  const moreBtnId = id.concat("-btn");

  const hiddenCommentElement = document.getElementById(hiddenId);
  const visibleCommentElement = document.getElementById(visibleId);

  hiddenCommentElement.classList.remove("hidden-element");
  visibleCommentElement.classList.add("hidden-element");

  document.getElementById(moreBtnId).classList.add("hidden-element");
}

async function removeComment(id) {
  try {
    const response = await fetch("/comment", { method: "DELETE", headers: { id } });

    getComments();
  } catch (e) {
    console.log("ERROR: ".concat(e));
  }
}

async function likeComment(id) {
  try {
    const response = await fetch("/comment/like", { method: 'POST', headers: { id } });

    getComments();
  } catch (e) {
    console.log("ERROR: ".concat(e));
  }
}

function createCommentsFromJson(payload) {
  console.log(payload);
  const comments = document.getElementById("comments");
  const template = document.getElementById("comment-template");
  
  comments.innerHTML = "";
  comments.appendChild(template);

  for (const comment of payload) {
    const commentToInsert = createCommentElement(comment);
    comments.append(commentToInsert);
    comments.append(document.createElement("br"));
  }
}

async function getComments() {
  const response = await fetch("/comment");
  const payload = await response.json();

  createCommentsFromJson(payload);
}