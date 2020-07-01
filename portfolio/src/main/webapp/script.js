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
 async function getUserQuestions() {
     console.log('Fetching user questions.');
     const response = await fetch('/data');
     const json = await response.json();
     const ranking_container = document.getElementById('questions-container');
     ranking_container.innerText = '';
     ranking_container.children = null;
     for(index in json) {
        ranking_container.appendChild(
            createListElement(json[index])
        );
    }
     
 }

 /** Creates an <li> element containing text. */
 function createListElement(text) {
    const liElement = document.createElement('li');
    liElement.innerText = text;
    return liElement;
 }
