// Set to false for faster local testing outside of the server.
// Prerequisite: Requires the Maven build to be run to make fixture files available.
const dynamicQuizUrl = true;

// Endpoints to invoke
const quizListUrl = '../quizzes';
var quizUrl = "../quiz";
var quizSubmissionUrl = "../gradedQuiz"

// Global quiz data shared between pages.
var quizListData = {};
var quizGroup = '';
var quizTitle = '';
var quizId = '';
var quizData = {};


/**
 * Update the quizUrl variable, if we currently want to use real (dynamic) endpoints.
 * If instead, we want to use static endpoints, for faster/easier UI testing, no update will be made.
 * @param  {String} quizTitle The quiz name to retrieve.
 * @return {String} The URL to use to retrieve the desired quiz.
 */
function updateQuizUrl(quizTitle) {
    if (dynamicQuizUrl == true) {
        quizUrl = "../quizzes/" + quizTitle;
    }
    return quizUrl;
}

/**
 * Update the quizSubmissionUrl variable, if we currently want to use real (dynamic) endpoints.
 * If instead, we want to use static endpoints, for faster/easier UI testing, no update will be made.
 * @return {String} The URL to use to submit the desired quiz.
 */
function updateQuizSubmissionUrl() {
    if (dynamicQuizUrl == true) {
        quizSubmissionUrl = "../quizzes/" + quizTitle + "/" + quizId;
    }
    return quizSubmissionUrl;
}


/**
 * Retrieve the quiz groupings from the server and update the groupings.html template display.
 */
function loadQuizGroupings(){
    // Make a GET request
    console.log('Retrieving quizzes: ', quizListUrl);
    fetch(quizListUrl)
      .then(response => {
        if (!response.ok) {
          console.log("Error", response);
          throw new Error(response.text());
        }
        return response.json();
      })
      .then(data => {
        quizListData = data;
        let quizListParent = document.querySelector('#quiz-grouping-list .intro').parentElement;

        for (const sectionName in data) {
            console.log(`${sectionName}: ${data[sectionName]}`);

            let listItemTitle = document.createElement('div');
            listItemTitle.classList.add("title");
            listItemTitle.classList.add("card_title");
            listItemTitle.textContent = sectionName;

            let listItem = document.createElement('ons-card');
            listItem.onclick = function() {
                quizGroup = sectionName;
                document.querySelector('#myNavigator').pushPage('quiz-list.html', {data: {title: 'Quiz'}});
            };
            listItem.append(listItemTitle);

            quizListParent.append(listItem);
        }
      })
      .catch(error => {
        console.error('Error:', error);
        showToast("Error loading quiz groupings.");
    });
}


/**
 * Retrieve the quiz groupings, if needed, from the server and update the quiz-list.html template display.
 */
function loadQuizList() {
    // Make a GET request
    if (quizListData == {}) {
        console.log('Retrieving quizzes: ', quizListUrl);
            fetch(quizListUrl)
              .then(response => {
                if (!response.ok) {
                  console.log("Error", response);
                  throw new Error(response.text());
                }
                return response.json();
              })
              .then(data => {
                console.log(data);
                quizListData = data;
              })
              .catch(error => {
                console.error('Error:', error);
                showToast("Error loading quiz list.");
            });
    }

    if (quizListData != {}) {
        let quizListParent = document.querySelector('#home-quiz-list .intro').parentElement;

        console.log(`${quizGroup}: ${quizListData[quizGroup]}`);

        for (let i = 0; i < quizListData[quizGroup].length; i++) {
            let listItemTitle = document.createElement('div');
            listItemTitle.classList.add("title");
            listItemTitle.classList.add("card_title");
            listItemTitle.textContent = quizListData[quizGroup][i];

            let listItem = document.createElement('ons-card');
            listItem.onclick = function() {
                document.querySelector('#myNavigator').pushPage('quiz.html', {data: {title: 'Quiz'}});
                quizTitle = quizListData[quizGroup][i];
            };
            listItem.append(listItemTitle);

            quizListParent.append(listItem);
        }
    }
}


/**
 * Retrieve the quiz details/questions from the server and update the quiz.html template display.
 */
function loadQuizQuestions() {
    document.querySelector('#forms-page .toolbar__title').textContent = quizTitle;

    updateQuizUrl(quizTitle);

    // Make a GET request
    console.log('Retrieving quiz: ', quizUrl);
    fetch(quizUrl)
      .then(response => {
        if (!response.ok) {
          console.log("Error", response);
          throw new Error(response.text());
        }
        return response.json();
      })
      .then(data => {
        console.log(data);
        quizData = data;
        quizId = data["id"];
        let promptList = data.prompts;
        let promptsParent = document.getElementById('question-list');
        for (let i = 0; i < promptList.length; i++) {
            let prompt = promptList[i];

            for (const key of Object.getOwnPropertyNames(prompt)) {
                let statusIcon = document.createElement('ons-icon');
                statusIcon.classList.add("list-item__icon");
                statusIcon.classList.add("ons-icon");
                statusIcon.classList.add("fa-square");

                let listItemCount = document.createElement('span');
                listItemCount.classList.add("prompt_count");
                listItemCount.textContent = (i + 1) + ") ";

                let listItemPrompt = document.createElement('span');
                listItemPrompt.textContent = key;

                let listItemQuestion = document.createElement('div');
                listItemQuestion.classList.add("left");
                listItemQuestion.classList.add("list-item__left");
                listItemQuestion.append(statusIcon);
                listItemQuestion.append(listItemCount);
                listItemQuestion.append(listItemPrompt);

                let listItemInput = document.createElement('input');
                listItemInput.classList.add('text-input');
                listItemInput.type = 'text';
                listItemInput.setAttribute('expected', prompt[key]);
                listItemInput.setAttribute('required', true);
                listItemInput.setAttribute('name', i.toString());
                listItemInput.placeholder = 'Answer';
                listItemInput.textContent = 'Answer';

                let listItemSpan = document.createElement('span');
                listItemSpan.classList.add('text-input__label');
                listItemSpan.textContent = 'Answer';

                let listItemOnsInput = document.createElement('ons-input');
                listItemOnsInput.id = "input-" + i;
                listItemOnsInput.placeholder = 'Answer';
                listItemOnsInput.classList.add("center");
                listItemOnsInput.setAttribute('expected', prompt[key]);
                listItemOnsInput.setAttribute('required', true);
                listItemOnsInput.setAttribute('name', i.toString());
                listItemOnsInput.append(listItemInput);
                listItemOnsInput.append(listItemSpan);
                listItemOnsInput.addEventListener('input', updateQuizProgress);

                let label = document.createElement('label');
                label.classList.add("center");
                label.classList.add("list-item__center");
                label.append(listItemOnsInput);

                let onsListItem = document.createElement('ons-list-item');
                onsListItem.id = "question-" + i;
                onsListItem.append(label);
                onsListItem.append(listItemQuestion);
                onsListItem.append(listItemOnsInput);

                promptsParent.append(onsListItem);

                // Should only contain one key/value pair
                break;
            }
        }
      })
      .catch(error => {
        console.error('Error:', error);
        showToast("Error retrieving quiz.");
    });

    let inlineGrading = document.getElementById('grade-quiz-inline').checked;
     if (inlineGrading) {
        document.getElementById('inline-grading-display-quiz').textContent = "Inline Grading: On"
     }  else {
        document.getElementById('inline-grading-display-quiz').textContent = "Inline Grading: Off"
     }
}


/**
 * Update the progress bar, submit button and per-prompt-status icons based upon the currently provided answers.
 * If grade-quiz-inline is requested, then will compare provided answer to expected answer.  If not, will only
 * ensure that an answer is populated.
 */
function updateQuizProgress() {
    let form = document.forms["quiz"];
    let progressBar = document.getElementById('quiz-progress');
    let promptCount = 0;
    let correctCount = 0;
    let answeredCount = 0;
    let inlineGrading = document.getElementById('grade-quiz-inline').checked;
    for (let i = 0; i < quizData.prompts.length; i++) {
        promptCount++;
        let inputElement = document.getElementById('input-' + i)
        let iconElement = document.querySelector('#question-' + i + " ons-icon");
        if (form[i].value != '') {
            answeredCount++;
            iconElement.setAttribute('icon', "fa-check, ion:ion-checkbox, material:md-check_box");
        } else {
            iconElement.setAttribute('icon', "fa-square, ion:ion-square, material:md-check_box_outline_blank");
        }
        if (inlineGrading) {
            if (form[i].getAttribute('expected') == form[i].value) {
                correctCount++;
                iconElement.classList.add("green");
            } else {
                iconElement.classList.remove("green");
            }
        }
    }
    let newProgressPercent = correctCount * 100 / promptCount;
    let answeredPercent = answeredCount * 100 / promptCount;
    progressBar.setAttribute('value', newProgressPercent);
    if (answeredPercent > 99){
        document.getElementById('quiz-submit').removeAttribute('disabled');
    } else {
        document.getElementById('quiz-submit').setAttribute('disabled', true);
    }
}


/**
 * Construct the JSON object that should be submitted to the server in order to grade the quiz.
 * @return {String} JSON object to submit to the server for the current quiz's answers.
 */
function generateCompletedQuiz() {
    let form = document.forms["quiz"];
    let completedQuizData = {
        'name': quizTitle,
        'inlineGrading': document.getElementById('grade-quiz-inline').checked,
        'answers': []
    }
    for (let i = 0; i < quizData.prompts.length; i++) {
        completedQuizData['answers'].push(form[i].value);
    }
    console.log("completedQuizData", completedQuizData);
    return completedQuizData;
}


/**
 * Call the server to grade the current quiz.  Update the score.html tempalte with the results.
 */
function scoreQuiz() {

    // Toolbar
    document.querySelector('#score-page .toolbar__title').textContent = quizTitle;
    let inlineGrading = document.getElementById('grade-quiz-inline').checked;
    if (inlineGrading) {
        document.getElementById('inline-grading-display-score').textContent = "Inline Grading: On"
    }  else {
        document.getElementById('inline-grading-display-score').textContent = "Inline Grading: Off"
    }

    // Submit quiz
    updateQuizSubmissionUrl();
    let requestBody = generateCompletedQuiz();
    let request = dynamicQuizUrl == true ?
                          {
                            'method': "POST",
                            'body': JSON.stringify(requestBody),
                            'headers': {
                              "Content-type": "application/json; charset=UTF-8"
                            }
                          } : {
                             'method': "GET"
                          };
    // Make a GET request
    console.log('Submitting quiz: ', quizSubmissionUrl);

    fetch(quizSubmissionUrl, request)
      .then(response => {
        console.log(response);
        console.log(response.body);
        if (!response.ok) {
          console.log("Error", response);
          throw new Error(response.text());
        }
        return response.json();
      })
      .then(data => {
        const now = new Date();
        const currentDateTimeStr = now.toLocaleString();
        document.querySelector('#datetime').textContent = currentDateTimeStr;

        document.getElementById('correctAnswers').textContent = data.correctAnswers;
        document.getElementById('totalPrompts').textContent = data.promptCount;
        document.getElementById('percentage').textContent = data.percentage;

        let duration = '';
        if (data.timeMinutes !== undefined && data.timeMinutes >= 1) {
            duration = duration + data.timeMinutes + " min ";
        }
        if (data.timeSeconds !== undefined && data.timeSeconds >= 1) {
            duration = duration + data.timeSeconds + " sec ";
        }
        document.getElementById('duration').textContent = duration;

        console.log(data);
      })
      .catch(error => {
        console.error('Error:', error);
        showToast("Error scoring quiz.");
    });
}


/**
 * Display a toast message with error details at the bottom of the page.
 */
function showToast(error) {
  ons.notification.toast({
    message: error,
    buttonLabel: "OK",
  });
}


