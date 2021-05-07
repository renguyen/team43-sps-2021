// Copyright 2020 Google LLC
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



/** Fetches comment list from the server and adds them to the page. */
async function getComments() {

  const responseFromServer = await fetch('/comment');
  const commentsFromResponse = await responseFromServer.text();
  const commentsListElement = document.getElementById('comment-block');

  commentsListElement.innerHTML = commentsFromResponse;
}

async function fetchMemeFile() {
  const responseFromServer = await fetch('/memes');
  const textFromResponse = await responseFromServer.text();
  const memeContainer = document.getElementById('memes-container');
  memeContainer.innerHTML = textFromResponse;
}
