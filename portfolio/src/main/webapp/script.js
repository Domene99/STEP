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

const COMMENT_LIMIT = 100;

function getRandomFact() {
  const facts =
    ["My ex podmate stole my go link", "I was the shortest person in my junior high", "I know 3 languages", "C++ is my favorite programming language"];

  const fact = facts[Math.floor(Math.random() * facts.length)];

  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

const createCommentElement = commentPayload => {
  console.log(commentPayload);
  const commentElement = document.createElement("li");
  const user = document.createElement("h4");
  const commentWrapper = document.createElement("div");
  const comment = document.createElement("p");
  const like = document.createElement("i");
  const likeIcon = document.createElement("span");
  const trash = document.createElement("i");
  const trashIcon = document.createElement("span");
  const numOfLikes = document.createElement("p");
  const bottomWrapper = document.createElement("div");

  const commentText = commentPayload.comment;
  const id = String(commentPayload.id);

  if (commentPayload.size <= COMMENT_LIMIT) {
    comment.innerHTML = commentText;
    commentWrapper.appendChild(comment);
  } else {
    const fullComment = document.createElement("p");
    const more = document.createElement("i");
    const moreIcon = document.createElement("span");

    moreIcon.classList.add("glyphicon", "glyphicon-triangle-bottom");

    const shortenedComment = commentText.slice(0, COMMENT_LIMIT);

    fullComment.innerHTML = commentText;

    comment.innerHTML = shortenedComment.concat("...");
    comment.id = id.concat("-visible");

    fullComment.classList.add("hidden-element");
    fullComment.id = id.concat("-hidden");

    more.appendChild(moreIcon);
    more.id = id.concat("-btn");
    more.onclick = () => showFullText(id);

    commentWrapper.appendChild(fullComment);
    commentWrapper.appendChild(comment);
    bottomWrapper.appendChild(more);
  }

  user.innerHTML = "@ ".concat(commentPayload.user);

  likeIcon.classList.add("glyphicon", "glyphicon-thumbs-up");
  trashIcon.classList.add("glyphicon", "glyphicon-trash");

  trash.appendChild(trashIcon);
  trash.onclick = () => removeComment(id);
  trash.classList.add("btn-right");

  const likes = parseInt(commentPayload.likes);

  numOfLikes.innerHTML = "+".concat(String(likes));
  numOfLikes.id = id.concat("-likes");
  numOfLikes.classList.add("btn-right");

  like.appendChild(likeIcon);
  like.onclick = () => likeComment(id);
  like.classList.add("btn-right");

  bottomWrapper.appendChild(trash);
  bottomWrapper.appendChild(like);
  bottomWrapper.appendChild(numOfLikes);

  commentElement.appendChild(user);
  commentElement.appendChild(commentWrapper);
  commentElement.appendChild(bottomWrapper);

  commentElement.id = id;
  commentElement.classList.add("list-group-item");

  return commentElement;
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
    const response = await fetch("/comment", { method: 'PUT', headers: { id } });

    getComments();
  } catch (e) {
    console.log("ERROR: ".concat(e));
  }
}

function createCommentsFromJson(payload) {
  const comments = document.getElementById("comments");
  comments.innerHTML = "";

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