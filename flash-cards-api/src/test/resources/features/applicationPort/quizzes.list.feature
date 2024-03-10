# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
@quizzes
Feature: Quiz - List

  Background:
    Given that my request uses the http protocol
    And that my request goes to the application port
    And that my request goes to endpoint quizzes
    And that my request uses the GET method

  Rule:  Input validation should be performed on all inputs consumed.

    @Component
    Scenario Outline: Failures - Unsupported Response Content Types
      Given that my request contains header Accept = <MIMEType>
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
    Scenario: Various Request Content Types
      Given that my request contains header Content-Type = application/json
      And that my request contains header Accept = application/json
      When I submit the request
      Then the response code is 200
      And the response body is "[\"Adding By 00\"]"

