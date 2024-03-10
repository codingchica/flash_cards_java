# See Gherkin syntax reference: https://cucumber.io/docs/gherkin/reference/
# If running this feature file directly (not part of the Maven build) you will need to
# set the following env/system properties that normally come from the Maven build:
# project.artifactId=${project.artifactId}
# project.version=${project.version}

@version
Feature: CLI Server

  @Component
  Scenario: CLI Server successful
    Given that my cli call includes the arguments
      | java                                                |
      | -jar                                                |
      | target/${project.artifactId}-${project.version}.jar |
      | server                                              |
      | src/test/resources/appConfig/test-component.yml     |
    When I run the CLI command until output contains: 'org.eclipse.jetty.server.Server: Started'
    Then the cli exit code is 1
    And CLI standard error is empty
    And CLI standard output contains the partial line 'org.eclipse.jetty.server.Server: Started'