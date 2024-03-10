# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
# If running this feature file directly (not part of the Maven build) you will need to
# set the following env/system properties that normally come from the Maven build:
# project.artifactId=${project.artifactId}
# project.version=${project.version}

@version
Feature: CLI Help

  @Component
  Scenario Outline: CLI Help successful
    Given that my cli call includes the arguments
      | java                                                |
      | -jar                                                |
      | target/${project.artifactId}-${project.version}.jar |
      | <Argument>                                          |
    When I run the CLI command until it stops
    Then the cli exit code is 0
    And CLI standard error is empty
    And CLI standard output matches the lines
      | usage: java -jar ${project.artifactId}-${project.version}.jar |
      | [-h] [-v] {server,check} ...                                  |
      |                                                               |
      | positional arguments:                                         |
      | {server,check}         available commands                     |
      |                                                               |
      | named arguments:                                              |
      | -h, --help             show this help message and exit        |
      | -v, --version          show the application version and exit  |
    Examples:
      | Argument |
      | -h       |
      | --help   |