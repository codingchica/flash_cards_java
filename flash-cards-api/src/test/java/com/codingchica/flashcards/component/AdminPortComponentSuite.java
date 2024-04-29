package com.codingchica.flashcards.component;

import static io.cucumber.junit.platform.engine.Constants.*;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * This is used by Cucumber to invoke the individual tests from the feature file(s) in the given
 * classpath directory. Additional settings are available in src/test/resources/cucumber.properties,
 * if desired. Those can either be specified using environment variables for the IDE or through the
 * CLI as an argument, if invoking cucumber directly using the CLI.
 *
 * <p>Only one class is used for the entire component test phase in order to more accurately report
 * all the feature files that were executed in the 'target/component-test-reports/report.html'.
 *
 * <p># If running this test directly (not part of the Maven build) you will need to # set the
 * following env/system properties that normally come from the Maven build: #
 * project.artifactId=${project.artifactId} # project.version=${project.version}
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource(
    // Folder from which test scenarios are retrieved from feature file(s) in this folder.
    // This could also point to an individual feature file, if desired.
    // Relies upon filtered properties / dynamic values
    "features/adminPort")
@ConfigurationParameter(
    // Package where the steps are defined for the tests in the feature file(s) selected.
    key = GLUE_PROPERTY_NAME,
    value = "com.codingchica.flashcards.component.steps.api")
@ConfigurationParameter(
    // Even if someone hasn't set up the IDE execution to read from the
    // src/test/resources/cucumber.properties file, (or corresponding environment variables), don't
    // output the banner asking them to publish the reports.
    key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME,
    value = "true")
public class AdminPortComponentSuite {
  // Actual tests are retrieved by reading the feature file(s) specified above.
  // No actual code is expected in this file.
}
