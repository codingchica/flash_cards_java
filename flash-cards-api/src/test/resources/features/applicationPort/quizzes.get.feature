# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
@quizzes
Feature: Quiz - Get

  Background:
    Given that my request uses the http protocol
    And that my request goes to the application port
    And that my request uses the GET method

  Rule:  Input validation should be performed on all inputs consumed.

    @Component
    Scenario: Failures - Quiz Not Found
      Given that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/DoesNotExist
      When I submit the request
      Then the response code is 404
      And the error response body contains JSON data
        | code    | 404                                     |
        | message | No match found for quiz: 'DoesNotExist' |

    @Component
    Scenario Outline: Failures - Unsupported Response Content Types
      Given that my request contains header Accept = <MIMEType>
      And that my request goes to endpoint quizzes/Adding%200
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

    @Component
    Scenario Outline: Various Request Content Types
      Given that my request contains header Content-Type = <MIMEType>
      And that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200
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

    @Component
    Scenario: Successful API call
      Given that my request contains header Content-Type = application/json
      And that my request contains header Accept = application/json
      And that my request goes to endpoint quizzes/Adding%200
      When I submit the request
      Then the response code is 200
      And the response body contains UUID at path(s)
        | id |
      And the response body contains JSON data
      # One copy of each expected response, as we aren't limiting by min or max
        | name               | Adding 0 |
        | prompts[*]["0+0"]  | ["0"]        |
        | prompts[*]["0+1"]  | ["1"]        |
        | prompts[*]["0+10"] | ["10"]       |
        | prompts[*]["0+11"] | ["11"]       |
        | prompts[*]["0+12"] | ["12"]       |
        | prompts[*]["0+2"]  | ["2"]        |
        | prompts[*]["0+3"]  | ["3"]        |
        | prompts[*]["0+4"]  | ["4"]        |
        | prompts[*]["0+5"]  | ["5"]        |
        | prompts[*]["0+6"]  | ["6"]        |
        | prompts[*]["0+7"]  | ["7"]        |
        | prompts[*]["0+8"]  | ["8"]        |
        | prompts[*]["0+9"]  | ["9"]        |
        | prompts[*]["1+0"]  | ["1"]        |
        | prompts[*]["10+0"] | ["10"]       |
        | prompts[*]["11+0"] | ["11"]       |
        | prompts[*]["12+0"] | ["12"]       |
        | prompts[*]["2+0"]  | ["2"]        |
        | prompts[*]["3+0"]  | ["3"]        |
        | prompts[*]["4+0"]  | ["4"]        |
        | prompts[*]["5+0"]  | ["5"]        |
        | prompts[*]["6+0"]  | ["6"]        |
        | prompts[*]["7+0"]  | ["7"]        |
        | prompts[*]["8+0"]  | ["8"]        |
        | prompts[*]["9+0"]  | ["9"]        |
