# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
@quizzes
@github
@Component
Feature: Quiz - Grade

  Background:
    Given that my request uses the http protocol
    And that my request goes to the application port
    And that my request uses the POST method
    And that my request is for a valid quiz ID


  Rule:  Input validation should be performed on all inputs consumed.

    Scenario: Failures - Quiz Not Found
      Given that my request contains header Content-Type = application/json
      Given that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200/{NEW_ID}
      And that my request body is '{"name":"Adding 0", "answers": []}'
      When I submit the request
      Then the response code is 404
      And the error response body contains JSON data
        | code    | 404                       |
        | message | Quiz='{NEW_ID}' not found |

    Scenario: Failures - Quiz ID Not UUID
      Given that my request contains header Content-Type = application/json
      Given that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200/MalformedUUID
      And that my request body is '{"name":"Adding 0", "answers": []}'
      When I submit the request
      Then the response code is 404
      And the error response body contains JSON data
        | code    | 404                |
        | message | HTTP 404 Not Found |

    Scenario: Failures - Quiz Name Mismatch
      Given that my request contains header Content-Type = application/json
      Given that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200/{ID}
      And that my request body is '{"name":"Adding 00", "answers": []}'
      When I submit the request
      Then the response code is 404
      And the error response body contains JSON data
        | code    | 404                |
        | message | Quiz name mismatch |

    Scenario Outline: Failures - Unsupported Request Content Types
      Given that my request contains header Content-Type = <MIMEType>
      Given that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200/{ID}
      And that my request body is '{"name":"Adding 0", "answers": []}'
      When I submit the request
      Then the response code is 415
      And the error response body contains JSON data
        | code    | 415                             |
        | message | HTTP 415 Unsupported Media Type |
      Examples:
        | MIMEType              |
        | application/ld+json   |
        | application/html      |
        | application/xhtml+xml |
        | application/zip       |
        | text/plain            |

    Scenario Outline: Failures - Unsupported Response Content Types
      Given that my request contains header Content-Type = application/json
      Given that my request contains header Accept = <MIMEType>
      And that my request goes to endpoint quizzes/Adding%200/{ID}
      And that my request body is '{"name":"Adding 0", "answers": []}'
      When I submit the request
      Then the response code is 406
      And the error response body contains JSON data
        | code    | 406                     |
        | message | HTTP 406 Not Acceptable |
      Examples:
        | MIMEType              |
        | application/ld+json   |
        | application/html      |
        | application/xhtml+xml |
        | application/zip       |
        | text/plain            |

    Scenario Outline: Various Request Content Types
      Given that my request contains header Content-Type = <MIMEType>
      And that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200/{ID}
      When I submit the request
      And the error response body contains JSON data
        | code    | 415                             |
        | message | HTTP 415 Unsupported Media Type |
      Examples:
        | MIMEType              |
        | application/ld+json   |
        | application/html      |
        | application/xhtml+xml |
        | application/zip       |
        | text/plain            |

  Rule:  When successful, the expected response should be returned.

    Scenario Outline: Quiz is correctly graded.
      Given that my request contains header Content-Type = application/json
      And that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200/{ID}
      And that my request body is for quiz 'Adding 0' with <correctAnswersCount> correct answers
      When I submit the request
      Then the response code is 200
      And the response body contains JSON data
        | name           | Adding 0              |
        | promptCount    | 25                    |
        | correctAnswers | <correctAnswersCount> |
        | percentage     | <percentage>          |
      Examples:
        # Unit test had percentage calculated, so hard-coding in case of calculation errors/rounding issues
        | correctAnswersCount | percentage |
        | 0                   | 0          |
        | 1                   | 4          |
        | 2                   | 8          |
        | 3                   | 12         |
        | 4                   | 16         |
        | 5                   | 20         |
        | 6                   | 24         |
        | 7                   | 28         |
        | 8                   | 32         |
        | 9                   | 36         |
        | 10                  | 40         |
        | 11                  | 44         |
        | 12                  | 48         |
        | 13                  | 52         |
        | 14                  | 56         |
        | 15                  | 60         |
        | 16                  | 64         |
        | 17                  | 68         |
        | 18                  | 72         |
        | 19                  | 76         |
        | 20                  | 80         |
        | 21                  | 84         |
        | 22                  | 88         |
        | 23                  | 92         |
        | 24                  | 96         |
        | 25                  | 100        |