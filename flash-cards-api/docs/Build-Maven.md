# Build - Maven

## Table of Contents

1. [Typical Local Usage](<a name="usage"></a>)
2. [Maven Default Lifecycle Behavior](<a name="defaultLifecycle"></a>)
    1. [Phase - validate](<a name="validate"></a>)
        1. [ID - auto-style](<a name="auto-style"></a>)
        2. [ID - enforce-style](<a name="enforce-style"></a>)
        3. [ID - enforce-dependency-convergence](<a name="enforce-dependency-convergence"></a>)
    2. [Phase - initialize](<a name="initialize"></a>)
        1. [ID - cucumber-properties](<a name="cucumber-properties"></a>)
        2. [ID - prepare-agent-ut](<a name="prepare-agent-ut"></a>)
    3. [Phase - generate-sources](<a name="generate-sources"></a>)
    4. [Phase - process-sources](<a name="process-sources"></a>)
    5. [Phase - generate-resources](<a name="generate-resources"></a>)
        1. [ID - default-resources](<a name="default-resources"></a>)
    6. [Phase - process-resources](<a name="process-resources"></a>)
    7. [Phase - compile](<a name="compile"></a>)
        1. [ID - default-compile](<a name="compile"></a>)
    8. [Phase - process-classes](<a name="process-classes"></a>)
    9. [Phase - generate-test-sources](<a name="generate-test-sources"></a>)
    10. [Phase - process-test-sources](<a name="process-test-sources"></a>)
    11. [Phase - generate-test-resources](<a name="generate-test-resources"></a>)
        1. [ID - default-testResources](<a name="default-testResources"></a>)
    12. [Phase - process-test-resources](<a name="process-test-resources"></a>)
    13. [Phase - test-compile](<a name="test-compile"></a>)
        1. [ID - default-testCompile](<a name="default-testCompile"></a>)
    14. [Phase - process-test-classes](<a name="process-test-classes"></a>)
    15. [Phase - test](<a name="test"></a>)
        1. [ID - default-test](<a name="default-test"></a>)
        2. [ID - report-ut](<a name="report-ut"></a>)
    16. [Phase - prepare-package](<a name="prepare-package"></a>)
    17. [Phase - package](<a name="package"></a>)
        1. [ID - default-jar](<a name="default-jar"></a>)
        2. [ID - shade-jar](<a name="shade-jar"></a>)
        3. [ID - sources-jar](<a name="sources-jar"></a>)
    18. [Phase - pre-integration-test](<a name="pre-integration-test"></a>)
        1. [ID - prepare-agent-component](<a name="prepare-agent-component"></a>)
    19. [Phase - integration-test](<a name="integration-test"></a>)
        1. [ID - component-tests](<a name="component-tests"></a>)
    20. [Phase - post-integration-test](<a name="post-integration-test"></a>)
        1. [ID - report-component](<a name="report-component"></a>)
    21. [Phase - verify](<a name="verify"></a>)
        1. [ID - jacoco-check-ut](<a name="jacoco-check-ut"></a>)
    22. [Phase - install](<a name="install"></a>)
        1. [ID - default-install](<a name="default-install"></a>)
    23. [Phase - deploy](<a name="deploy"></a>)

<a name="usage"></a>

## Typical Local Usage

`mvn install` - Executes the `install` lifecycle phase and all prior phases.

<a name="defaultLifecycle"></a>

## Maven Default Lifecycle Behavior

The [Default lifecycle for a Maven build](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#default-lifecycle)
consists of the following phases:

1. validate
2. initialize
3. generate-sources
4. process-sources
5. generate-resources
6. process-resources
7. compile
8. process-classes
9. generate-test-sources
10. process-test-sources
11. generate-test-resources
12. process-test-resources
13. test-compile
14. process-test-classes
15. test
16. prepare-package
17. package
18. pre-integration-test
19. integration-test
20. post-integration-test
21. verify
22. install
23. deploy

When issuing a `mvn install` command, steps 1-22 will execute.

<a name="validate"></a>

### 1. Phase - validate

<a name="auto-style"></a>

#### 1.1 ID - auto-style

The [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven) is used to automatically apply
certain styles to the code and configuration.

<a name="enforce-style"></a>

#### 1.2 ID - enforce-style

The [Maven Checkstyle Plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is used to enforce the
built-in [google_checks.xml](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml)
styling.

<a name="enforce-dependency-convergence"></a>

#### 1.3 ID - enforce-dependency-convergence

The [Maven Enforcer Plugin](https://maven.apache.org/enforcer/maven-enforcer-plugin/) is used to enforce that
dependencies converge.

See [Dependency Convergence](https://maven.apache.org/enforcer/enforcer-rules/dependencyConvergence.html) for additional
details.

<a name="initialize"></a>

### 2. Phase - initialize

<a name="cucumber-properties"></a>

#### 2.1 ID - cucumber-properties

The [Properties Maven Plugin](https://www.mojohaus.org/properties-maven-plugin/) reads in
the `src/test/resources/cucumber.properties` to aid in subsequent Behavior Driven Development (BDD) testing.

<a name="prepare-agent-ut"></a>

#### 2.1 ID - prepare-agent-ut

The [Jacoco Maven Plugin](https://www.eclemma.org/jacoco/trunk/doc/prepare-agent-mojo.html) prepares the unit test code
coverage monitoring agent.

<a name="generate-sources"></a>

### 3. Phase - generate-sources

<a name="process-sources"></a>

### 4. Phase - process-sources

<a name="generated-resources"></a>

### 5. Phase - generate-resources

<a name="process-resources"></a>

### 6. Phase - process-resources

<a name="default-resources"></a>

#### 6.1 ID - default-resources

The [Maven Resources Plugin](https://maven.apache.org/plugins/maven-resources-plugin/) copies sr/min/resources files to
the build directory.

<a name="compile"></a>

### 7. Phase - compile

<a name="compile"></a>

#### 7.1 ID - default-compile

The [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html) compiles the Java
src/main/resources code base.

<a name="process-classes"></a>

### 8. Phase - process-classes

<a name="generate-test-sources"></a>

### 9. Phase - generate-test-sources

<a name="default-testResources"></a>

#### 9.1 ID - default-testResources

The [Maven Resources Plugin](https://maven.apache.org/plugins/maven-resources-plugin/) copies sr/test/resources files to
the build directory.

<a name="process-test-sources"></a>

### 10. Phase - process-test-sources

<a name="generate-test-resources"></a>

### 11. ID - generate-test-resources

<a name="process-test-resources"></a>

### 12. Phase - process-test-resources

<a name="test-compile"></a>

### 13. Phase - test-compile

<a name="default-testCompile"></a>

#### 13.1 ID - default-testCompile

The [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html) compiles the Java
src/test/resources code base.

<a name="process-test-classes"></a>

### 14. Phase - process-test-classes

<a name="test"></a>

### 15. Phase - test

<a name="default-test"></a>

#### 15.1 ID - default-test

The [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html) executes the unit
test (like `*Test.java`) cases.  
The build will be marked as failed if either:

- No tests are found, or
- Any test validations fail.

<a name="report-ut"></a>

#### 15.2 ID - report-ut

The [Jacoco Maven Plugin](https://www.eclemma.org/jacoco/trunk/doc/prepare-agent-mojo.html) generates the code coverage
report in `target/ut-coverage-reports/`.

<a name="prepare-package"></a>

### 16. Phase - prepare-package

<a name="package"></a>

### 17. Phase - package

<a name="default-jar"></a>

#### 17.1 ID - default-jar

The [Maven Jar Plugin](https://maven.apache.org/plugins/maven-jar-plugin/jar-mojo.html) builds the regular jar in
the `target` directory.

<a name="shade-jar"></a>

#### 17.2 ID - shade-jar

The [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/shade-mojo.html) creates the shaded jar in
the `target` directory.

<a name="sources-jar"></a>

#### 17.3 ID - sources-jar

The [Maven Source Plugin](https://maven.apache.org/plugins/maven-source-plugin/jar-mojo.html) creates the sources jar in
the `target` directory.

TODO - Why does the log output look like the validate section/generate-sources is repeating in this section?

<a name="pre-integration-test"></a>

### 18. Phase - pre-integration-test

<a name="prepare-agent-component"></a>

#### 18.1 ID - prepare-agent-component

The [Jacoco Maven Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html) plugin prepares the code coverage agent
to collect code coverage data for component tests.

<a name="integration-test"></a>

### 19. Phase - integration-test

<a name="component-tests"></a>

#### 19.1 ID - component-tests

The [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/) is used once again to run
component level tests.  This was chosen to consistently and easily enforce that no component tests / failing component tests also cause the build to fail.

This phase is based upon [Gherkin](https://cucumber.io/docs/gherkin/reference/) feature files and runs using the [Cucumber](https://cucumber.io/docs/guides/overview/) test engine for Behavior Drive Development (BDD).

<a name="post-integration-test"></a>

### 20. Phase - post-integration-test

<a name="report-component"></a>

#### 20.1 ID - report-component
The [Jacoco Maven Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html) generates a component test code coverage report in the `target/component-coverage-reports/` folder.

<a name="verify"></a>

### 21. Phase - verify

<a name="jacoco-check-ut"></a>

#### 21.1 ID - jacoco-check-ut

The [Jacoco Maven Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html) enforces that minimum code coverage
requirements have been met for each class:

| Class Counter  | Minimum Percentage |
|----------------|--------------------|
| Lines          | 90%                |
| Logic Branches | 90%                |

<a name="install"></a>

### 22. Phase - install

<a name="default-install"></a>

#### 22.1 ID - default-install

The [Maven Install Plugin](https://maven.apache.org/plugins/maven-install-plugin/install-mojo.html) Install the built
artifacts into the local Maven repository (local Maven cache).

<a name="deploy"></a>

### 23. Phase - deploy