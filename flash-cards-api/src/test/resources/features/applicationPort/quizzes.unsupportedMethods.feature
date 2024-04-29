# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
@quizzes
@github
@Component
Feature: Quizzes API - Unsupported Methods

  Background:
    Given that my request uses the http protocol
    And that my request goes to the application port

  Scenario Outline: List Quizzes - 405 Method Not Supported
    Given that my request uses the <HTTPMethod> method
    And that my request goes to endpoint quizzes
    When I submit the request
    Then the response code is 405
    Examples:
      | HTTPMethod |
      | POST       |
      | PUT        |
      | TRACE      |
      | DELETE     |

  Scenario Outline: Get Quiz - 405 Method Not Supported
    Given that my request uses the <HTTPMethod> method
    And that my request goes to endpoint quizzes/Adding%20By%2000
    When I submit the request
    Then the response code is 405
    Examples:
      | HTTPMethod |
      | POST       |
      | PUT        |
      | TRACE      |
      | DELETE     |


  Scenario Outline: Grade Quiz - 405 Method Not Supported
    Given that my request uses the <HTTPMethod> method
    And that my request is for a valid quiz ID
    And that my request goes to endpoint quizzes/Adding%200/{ID}
    When I submit the request
    Then the response code is 405
    Examples:
      | HTTPMethod |
      | GET       |
      | PUT        |
      | TRACE      |
      | DELETE     |

