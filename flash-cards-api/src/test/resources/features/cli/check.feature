# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
# If running this feature file directly (not part of the Maven build) you will need to
# set the following env/system properties that normally come from the Maven build:
# project.artifactId=${project.artifactId}
# project.version=${project.version}

@version
Feature: CLI Check Configuration File

  @Component
  Scenario Outline: CLI confirms configuration file OK
    Given that my cli call includes the arguments
      | java                                                |
      | -jar                                                |
      | target/${project.artifactId}-${project.version}.jar |
      | check                                               |
      | <Configuration File>                                |
    When I run the CLI command until it stops
    Then the cli exit code is 0
    And CLI standard error is empty
    And CLI standard output contains the partial line 'io.dropwizard.core.cli.CheckCommand: Configuration is OK'
    Examples:
      | Configuration File                              |
      | src/test/resources/appConfig/test-component.yml |
      | src/main/resources/appConfig/prod.yml           |