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

/**
 * Adds a random fun fact to the page.
 */
function getFunFact() {
  const facts =
      ['I love Mike Tyson Mysteries.', 'I was once attacked by a turkey.', 
      'I can\'t say the alphabet backwards.', 'I\'ve seen the original Predator ~100 times.', 
      'I played almost 400 hours of Mount and Blade Warband in middle school.',
      'I once coded a neural net in Java from scratch and got it to 92% accuracy on MNIST.'];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/**
    Gets and displays my bottled water rankings from /water-rankings.
 */
 async function getWaterRankings() {
     console.log('Fetching water ratings.');
     const response = await fetch('/water-rankings');
     const json = await response.json();
     const ranking_container = document.getElementById('water-ranking-container');
     ranking_container.innerText = '';
     ranking_container.children = null;
     for(index in json) {
        ranking_container.appendChild(
            createListElement(json[index])
        );
    }
     
 }

 /**
    Gets and displays user questions from /data.
 */
 function getUserQuestions() {
     const questionsShown = document.getElementById("questions-shown-selector");
     const questionCount = questionsShown.value;
     const fetchParameter = "/data?count=" + questionCount;
     const response = fetch(fetchParameter);
     response.then(getQuestionJson);
 }

 /** Gets the json from the fetched questions */
 function getQuestionJson(response) {
     const json = response.json();
     json.then(populateQuestions);
 }

 /** Populates the user submitted quesitons. */
 function populateQuestions(json) {
     const questionsShown = document.getElementById("questions-shown-selector");
     const ranking_container = document.getElementById('questions-container');
     ranking_container.innerText = '';
     ranking_container.children = null;
     const questionCount = questionsShown.value;
     console.log(questionCount);
     for(index in json) {
        ranking_container.appendChild(
            createListElement(json[index])
        );
    }
 }

 /** Function to be run on loading of the page. */
 function startPage() {
     const question_submit_button = document.getElementById("question-submit-button");
     question_submit_button.hidden = true;
     getUserQuestions();
 }

 /** Checks if the submit button can be displayed. */
 function checkQuestionSubmitButton() {
     const question_input = document.getElementById('question-input');
     const question_submit_button = document.getElementById("question-submit-button");
     const question_content = question_input.value;
     if (question_content == null || question_content == "") {
        question_submit_button.hidden = true;
     } else {
         question_submit_button.hidden = false;
     }
 }

 /** Submits the user's question to the server. */
 function submitUserQuestion() {
     const question_input = document.getElementById('question-input');
     const question_content = question_input.value;
     question_input.value = "";
     const question_submit_button = document.getElementById("question-submit-button");
     question_submit_button.hidden = true;
     if (question_content == null || question_content == "") {
        /** This case shouldn't be reachable as the submit button is only displayed when the text box furfills these conditions. */
     } else {
        fetch("/data?content="+question_content, {method: 'POST'}).then(getUserQuestions);
     }
 }

 /** Deletes all database entries. */
 function deleteDatabaseEntries() {
     console.log("deleting data");
     fetch("/delete-data", {method: 'POST'}).then(getUserQuestions);
 }

 /** Creates an <li> element containing text. */
 function createListElement(text) {
    const liElement = document.createElement('li');
    liElement.innerText = text;
    return liElement;
 }
