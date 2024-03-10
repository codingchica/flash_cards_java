# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
@ping
Feature: Ping API

  Background:
    Given that my request uses the http protocol
    And that my request goes to the admin port
    And that my request goes to endpoint ping

  @Component
  Scenario Outline: GET call returns successful response.
    Given that my request contains header Content-Type = <expectedResponseType>
    And that my request contains header Accept = <expectedResponseType>
    And that my request uses the GET method
    When I submit the request
    Then the response code is 200
    And the response body is pong
    Examples:
      | expectedResponseType |
      | application/json     |
      | application/html     |

  @Component
  Scenario Outline: HEAD/OPTIONS call returns successful response.
    Given that my request uses the <HTTPMethod> method
    When I submit the request
    Then the response code is 200
    And the response body is completely empty
    Examples:
      | HTTPMethod |
      | HEAD       |
      | OPTIONS    |

  @Component
  Scenario Outline: Unsupported methods.
    Given that my request uses the <HTTPMethod> method
    When I submit the request
    Then the response code is 405
    Examples:
      | HTTPMethod |
      | POST       |
      | PUT        |
      | TRACE      |
      | DELETE     |