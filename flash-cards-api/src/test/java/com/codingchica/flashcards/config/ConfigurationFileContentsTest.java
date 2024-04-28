package com.codingchica.flashcards.config;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.codahale.metrics.annotation.ResponseMeteredLevel;
import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.configuration.*;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.server.ServerFactory;
import io.dropwizard.core.setup.AdminFactory;
import io.dropwizard.core.setup.HealthCheckConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.GzipHandlerFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.ServerPushFilterFactory;
import io.dropwizard.logging.common.AppenderFactory;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.DefaultLoggingFactory;
import io.dropwizard.logging.common.LoggingFactory;
import io.dropwizard.metrics.common.MetricsFactory;
import io.dropwizard.metrics.common.ReporterFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.request.logging.RequestLogFactory;
import io.dropwizard.servlets.tasks.TaskConfiguration;
import io.dropwizard.util.DataSize;
import jakarta.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.UriCompliance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;

/**
 * Tests to enforce that all application configuration files contain the expected contents, once
 * deserialized into POJO(s). See also: <br>
 * https://www.dropwizard.io/en/latest/manual/testing.html#testing-configurations
 *
 * <p>When running this test locally, you must set the expected environment variables to match the
 * Maven build.
 */
public class ConfigurationFileContentsTest {

  private static final ObjectMapper objectMapper = Jackson.newObjectMapper();
  private static final Validator validator = Validators.newValidator();

  @Spy
  private final EnvironmentVariableSubstitutor environmentVariableSubstitutor =
      new EnvironmentVariableSubstitutor(true);

  private final YamlConfigurationFactory<FlashCardsConfiguration> factory =
      new YamlConfigurationFactory<>(FlashCardsConfiguration.class, validator, objectMapper, "dw");

  private final ResourceConfigurationSourceProvider resourceConfigurationSourceProvider =
      new ResourceConfigurationSourceProvider();

  private static Stream<Arguments> configFiles = null;
  /**
   * A collection to allow tests that should include validation every field to enforce that new
   * fields are also added.
   */
  private final Set<String> testedFields = new TreeSet<>();

  private final Set<String> expectedFieldNames = new TreeSet<>();

  private boolean isTestConfig = false;

  /**
   * These are set up in the Maven pom.xml. If you are running the test locally in an IDE, you must
   * also set these values in your run configuration.
   */
  @BeforeAll
  public static void enforceEnvironmentSetup() {
    Map<String, String> expectedEnvironmentVariables = new TreeMap<>();

    expectedEnvironmentVariables.put("LOG_LEVEL_MAIN", "INFO");

    expectedEnvironmentVariables.forEach(
        (key, value) ->
            assertEquals(
                value, System.getenv(key), key + " is not setup in environment variables"));

    objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
  }

  private static Stream<Arguments> getConfigFilesInDirectory(File directory, boolean isTestConfig) {

    assertTrue(directory.exists(), directory.getPath() + " does not exist");
    assertTrue(directory.isDirectory(), directory.getPath() + " is not a directory");
    return Arrays.stream(
        Objects.requireNonNull(
            directory.listFiles(
                (dir, name) ->
                    StringUtils.endsWithIgnoreCase(name, ".yml")
                        || StringUtils.endsWithIgnoreCase(name, ".yaml"))))
        // Classloader is used at runtime to retrieve file contents.
        .map(
            (item) ->
                Arguments.of(
                    StringUtils.substring(
                        item.getPath(), StringUtils.indexOf(item.getPath(), "appConfig")),
                    isTestConfig))
        .toList()
        .stream();
  }

  /**
   * Retrieve all YAML files from the configuration directory: src/main/resources/appConfig.
   *
   * @return The file paths to available configuration files.
   */
  public static Stream<Arguments> provideConfigFiles() {
    if (configFiles == null) {
      configFiles =
          Stream.concat(
              getConfigFilesInDirectory(new File("src/main/resources/appConfig"), false),
              getConfigFilesInDirectory(new File("src/test/resources/appConfig"), true));
    }
    return configFiles;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideConfigFiles")
  public void testFileContents(@NonNull String configFilePath, boolean isTestConfig)
      throws ConfigurationException, IOException {
    // Setup
    this.isTestConfig = isTestConfig;
    String prefix = "appConfig";
    expectClassFieldsTested(prefix, FlashCardsConfiguration.class);
    SubstitutingSourceProvider substitutingSourceProvider =
        new SubstitutingSourceProvider(
            resourceConfigurationSourceProvider, environmentVariableSubstitutor);

    // Execution
    FlashCardsConfiguration configPOJO = factory.build(substitutingSourceProvider, configFilePath);

    // Validation
    // Check immediate filed status
    assertAll(
        // Dropwizard fields
        () -> assertNotNullAndLog(configPOJO.getAdminFactory(), prefix + ".adminFactory"),
        () -> assertTrueAndLog(configPOJO.getHealthFactory().isEmpty(), prefix + ".healthFactory"),
        () -> assertNotNullAndLog(configPOJO.getLoggingFactory(), prefix + ".loggingFactory"),
        () -> assertNotNullAndLog(configPOJO.getMetricsFactory(), prefix + ".metricsFactory"),
        () -> assertNotNullAndLog(configPOJO.getServerFactory(), prefix + ".serverFactory"),
        // My fields
        () ->
            assertNotNullAndLog(configPOJO.getFlashCardGroupMap(), prefix + ".flashCardGroupMap"));
    assertAllFieldsUsedAndClear();

    // Drill into nested objects for validations.
    // Dropwizard fields
    testContents(configPOJO.getAdminFactory());
    testContents(configPOJO.getLoggingFactory());
    testContents(configPOJO.getMetricsFactory());
    testContents(configPOJO.getServerFactory());
    // My fields
    if (isTestConfig) {
      testContents_FlashCardGroupMapTest(configPOJO.getFlashCardGroupMap());
    } else {
      testContents_FlashCardGroupMapProd(configPOJO.getFlashCardGroupMap());
    }
  }

  public void testContents_FlashCardGroupMapProd(
      @NonNull Map<String, List<FlashCardGroup>> flashCardGroupMap) {
    // Setup
    int minPrompts = 20;
    int maxPrompts = 20;
    String[] expectedKeys =
        new String[] {
          "Addition", "Division", "Multiplication", "Subtraction",
        };
    flashCardGroupMap.forEach(
        (key, value) ->
            value.stream()
                .forEach(item -> expectClassFieldsTested(item.getName(), FlashCardGroup.class)));

    // Validation
    // Immediate fields
    assertEquals(
        Arrays.stream(expectedKeys).toList(),
        flashCardGroupMap.keySet().stream().sorted().collect(Collectors.toList()),
        "flashCardGroupMap.keySet");
    List<FlashCardGroup> addition = flashCardGroupMap.get("Addition");
    List<FlashCardGroup> subtraction = flashCardGroupMap.get("Subtraction");
    List<FlashCardGroup> multiplication = flashCardGroupMap.get("Multiplication");
    List<FlashCardGroup> division = flashCardGroupMap.get("Division");
    assertAll(
        () -> testAdding("Adding 0", addition, 0, minPrompts, maxPrompts),
        () -> testAdding("Adding 1", addition, 1, minPrompts, maxPrompts),
        () -> testAdding("Adding 2", addition, 2, minPrompts, maxPrompts),
        () -> testAdding("Adding 3", addition, 3, minPrompts, maxPrompts),
        () -> testAdding("Adding 4", addition, 4, minPrompts, maxPrompts),
        () -> testAdding("Adding 5", addition, 5, minPrompts, maxPrompts),
        () -> testAdding("Adding 6", addition, 6, minPrompts, maxPrompts),
        () -> testAdding("Adding 7", addition, 7, minPrompts, maxPrompts),
        () -> testAdding("Adding 8", addition, 8, minPrompts, maxPrompts),
        () -> testAdding("Adding 9", addition, 9, minPrompts, maxPrompts),
        () -> testAdding("Adding 10", addition, 10, minPrompts, maxPrompts),
        () -> testAdding("Adding 11", addition, 11, minPrompts, maxPrompts),
        () -> testAdding("Adding 12", addition, 12, minPrompts, maxPrompts),
        () -> testSubtraction("Subtracting 0", subtraction, 0),
        () -> testSubtraction("Subtracting 1", subtraction, 1),
        () -> testSubtraction("Subtracting 2", subtraction, 2),
        () -> testSubtraction("Subtracting 3", subtraction, 3),
        () -> testSubtraction("Subtracting 4", subtraction, 4),
        () -> testSubtraction("Subtracting 5", subtraction, 5),
        () -> testSubtraction("Subtracting 6", subtraction, 6),
        () -> testSubtraction("Subtracting 7", subtraction, 7),
        () -> testSubtraction("Subtracting 8", subtraction, 8),
        () -> testSubtraction("Subtracting 9", subtraction, 9),
        () -> testSubtraction("Subtracting 10", subtraction, 10),
        () -> testSubtraction("Subtracting 11", subtraction, 11),
        () -> testSubtraction("Subtracting 12", subtraction, 12),
        () -> testMultiplication("Multiplying By 0", multiplication, 0),
        () -> testMultiplication("Multiplying By 1", multiplication, 1),
        () -> testMultiplication("Multiplying By 2", multiplication, 2),
        () -> testMultiplication("Multiplying By 3", multiplication, 3),
        () -> testMultiplication("Multiplying By 4", multiplication, 4),
        () -> testMultiplication("Multiplying By 5", multiplication, 5),
        () -> testMultiplication("Multiplying By 6", multiplication, 6),
        () -> testMultiplication("Multiplying By 7", multiplication, 7),
        () -> testMultiplication("Multiplying By 8", multiplication, 8),
        () -> testMultiplication("Multiplying By 9", multiplication, 9),
        () -> testMultiplication("Multiplying By 10", multiplication, 10),
        () -> testMultiplication("Multiplying By 11", multiplication, 11),
        () -> testMultiplication("Multiplying By 12", multiplication, 12),
        () -> testDivision("Dividing By 1", division, 1),
        () -> testDivision("Dividing By 2", division, 2),
        () -> testDivision("Dividing By 3", division, 3),
        () -> testDivision("Dividing By 4", division, 4),
        () -> testDivision("Dividing By 5", division, 5),
        () -> testDivision("Dividing By 6", division, 6),
        () -> testDivision("Dividing By 7", division, 7),
        () -> testDivision("Dividing By 8", division, 8),
        () -> testDivision("Dividing By 9", division, 9),
        () -> testDivision("Dividing By 10", division, 10),
        () -> testDivision("Dividing By 11", division, 11),
        () -> testDivision("Dividing By 12", division, 12));

    assertAllFieldsUsedAndClear();
    // No nested objects
  }

  public void testContents_FlashCardGroupMapTest(
      @NonNull Map<String, List<FlashCardGroup>> flashCardGroupMap) {
    // Setup
    String prefix = "flashCardGroupMap";
    String[] expectedKeys =
        new String[] {
          "Addition",
        };
    flashCardGroupMap.forEach(
        (key, value) ->
            value.stream()
                .forEach(item -> expectClassFieldsTested(item.getName(), FlashCardGroup.class)));

    // Validation
    // Immediate fields
    assertEquals(
        Arrays.stream(expectedKeys).toList(),
        flashCardGroupMap.keySet().stream().sorted().collect(Collectors.toList()),
        "flashCardGroupMap.keySet");
    List<FlashCardGroup> addition = flashCardGroupMap.get("Addition");
    assertAll(() -> testAdding("Adding 0", addition, 0, 0, 0));

    assertAllFieldsUsedAndClear();
    // No nested objects
  }

  public void testAdding(
      String name,
      @NonNull List<FlashCardGroup> flashCardGroups,
      int addend,
      int minPrompts,
      int maxPrompts) {
    // Setup
    Optional<FlashCardGroup> optionalQuiz =
        flashCardGroups.stream()
            .filter(quiz -> StringUtils.equals(quiz.getName(), name))
            .findFirst();
    assertFalse(
        optionalQuiz.isEmpty(),
        () ->
            "'"
                + name
                + "' quiz not found in "
                + flashCardGroups.stream().map(FlashCardGroup::getName).toList());
    FlashCardGroup flashCardGroup = optionalQuiz.get();
    Optional<Map<String, String>> expectedPrompts =
        IntStream.range(0, 13)
            .mapToObj(
                value -> {
                  Map<String, String> prompts = new TreeMap<>();
                  prompts.put(
                      String.format("%s+%s", value, addend), String.format("%s", value + addend));
                  prompts.put(
                      String.format("%s+%s", addend, value), String.format("%s", value + addend));
                  return prompts;
                })
            .reduce(
                (stringStringMap, stringStringMap2) -> {
                  stringStringMap.putAll(stringStringMap2);
                  return stringStringMap;
                });
    assertTrue(expectedPrompts.isPresent());

    // Validation
    assertAll(
        () ->
            assertEqualsAndLog(
                expectedPrompts.get(), flashCardGroup.getPrompts(), name + ".prompts"),
        () ->
            assertEqualsAndLog(
                minPrompts, flashCardGroup.getMaximumPrompts(), name + ".maximumPrompts"),
        () ->
            assertEqualsAndLog(
                maxPrompts, flashCardGroup.getMinimumPrompts(), name + ".minimumPrompts"),
        () -> assertEqualsAndLog(name, flashCardGroup.getName(), name + ".name"));
  }

  public void testSubtraction(
      String name, @NonNull List<FlashCardGroup> flashCardGroups, int addend) {
    // Setup
    Optional<FlashCardGroup> optionalQuiz =
        flashCardGroups.stream()
            .filter(quiz -> StringUtils.equals(quiz.getName(), name))
            .findFirst();
    assertFalse(
        optionalQuiz.isEmpty(),
        () ->
            "'"
                + name
                + "' quiz not found in "
                + flashCardGroups.stream().map(FlashCardGroup::getName).toList());
    FlashCardGroup flashCardGroup = optionalQuiz.get();
    Optional<Map<String, String>> expectedPrompts =
        IntStream.range(0 + addend, 13 + addend)
            .mapToObj(
                value -> {
                  Map<String, String> prompts = new TreeMap<>();
                  prompts.put(
                      String.format("%s-%s", value, addend), String.format("%s", value - addend));
                  return prompts;
                })
            .reduce(
                (stringStringMap, stringStringMap2) -> {
                  stringStringMap.putAll(stringStringMap2);
                  return stringStringMap;
                });
    assertTrue(expectedPrompts.isPresent());

    // Validation
    assertAll(
        () ->
            assertEqualsAndLog(
                expectedPrompts.get(), flashCardGroup.getPrompts(), name + ".prompts"),
        () -> assertEqualsAndLog(20, flashCardGroup.getMaximumPrompts(), name + ".maximumPrompts"),
        () -> assertEqualsAndLog(20, flashCardGroup.getMinimumPrompts(), name + ".minimumPrompts"),
        () -> assertEqualsAndLog(name, flashCardGroup.getName(), name + ".name"));
  }

  public void testMultiplication(
      String name, @NonNull List<FlashCardGroup> flashCardGroups, int factor) {
    // Setup
    Optional<FlashCardGroup> optionalQuiz =
        flashCardGroups.stream()
            .filter(quiz -> StringUtils.equals(quiz.getName(), name))
            .findFirst();
    assertFalse(
        optionalQuiz.isEmpty(),
        () ->
            "'"
                + name
                + "' quiz not found in "
                + flashCardGroups.stream().map(FlashCardGroup::getName).toList());
    FlashCardGroup flashCardGroup = optionalQuiz.get();
    Optional<Map<String, String>> expectedPrompts =
        IntStream.range(0, 13)
            .mapToObj(
                value -> {
                  Map<String, String> prompts = new TreeMap<>();
                  prompts.put(
                      String.format("%sx%s", value, factor), String.format("%s", value * factor));
                  return prompts;
                })
            .reduce(
                (stringStringMap, stringStringMap2) -> {
                  stringStringMap.putAll(stringStringMap2);
                  return stringStringMap;
                });
    assertTrue(expectedPrompts.isPresent());

    // Validation
    assertAll(
        () ->
            assertEqualsAndLog(
                expectedPrompts.get(), flashCardGroup.getPrompts(), name + ".prompts"),
        () -> assertEqualsAndLog(20, flashCardGroup.getMaximumPrompts(), name + ".maximumPrompts"),
        () -> assertEqualsAndLog(20, flashCardGroup.getMinimumPrompts(), name + ".minimumPrompts"),
        () -> assertEqualsAndLog(name, flashCardGroup.getName(), name + ".name"));
  }

  public void testDivision(String name, @NonNull List<FlashCardGroup> flashCardGroups, int factor) {
    // Setup
    Optional<FlashCardGroup> optionalQuiz =
        flashCardGroups.stream()
            .filter(quiz -> StringUtils.equals(quiz.getName(), name))
            .findFirst();
    assertFalse(
        optionalQuiz.isEmpty(),
        () ->
            "'"
                + name
                + "' quiz not found in "
                + flashCardGroups.stream().map(FlashCardGroup::getName).toList());
    FlashCardGroup flashCardGroup = optionalQuiz.get();
    Optional<Map<String, String>> expectedPrompts =
        IntStream.range(0, 13)
            .mapToObj(
                value -> {
                  Map<String, String> prompts = new TreeMap<>();
                  prompts.put(
                      String.format("%s/%s", value * factor, factor),
                      String.format("%s", value * factor / factor));
                  return prompts;
                })
            .reduce(
                (stringStringMap, stringStringMap2) -> {
                  stringStringMap.putAll(stringStringMap2);
                  return stringStringMap;
                });
    assertTrue(expectedPrompts.isPresent());

    // Validation
    assertAll(
        () ->
            assertEqualsAndLog(
                expectedPrompts.get(), flashCardGroup.getPrompts(), name + ".prompts"),
        () -> assertEqualsAndLog(20, flashCardGroup.getMaximumPrompts(), name + ".maximumPrompts"),
        () -> assertEqualsAndLog(20, flashCardGroup.getMinimumPrompts(), name + ".minimumPrompts"),
        () -> assertEqualsAndLog(name, flashCardGroup.getName(), name + ".name"));
  }

  public void testContents(@NonNull MetricsFactory metrics) {
    // Setup
    String prefix = "metrics";
    expectClassFieldsTested(prefix, MetricsFactory.class);

    // Validation
    // Immediate fields
    assertAll(
        () ->
            assertEqualsAndLog(
                io.dropwizard.util.Duration.minutes(1),
                metrics.getFrequency(),
                prefix + ".frequency"),
        () ->
            assertEqualsAndLog(
                new ArrayList<ReporterFactory>(), metrics.getReporters(), prefix + ".reporters"),
        () -> assertFalseAndLog(metrics.isReportOnStop(), prefix + ".reportOnStop"));
    assertAllFieldsUsedAndClear();
    // No nested objects
  }

  public void testContents(@NonNull AdminFactory adminFactory) {
    // Setup
    String prefix = "adminFactory";
    expectClassFieldsTested(prefix, AdminFactory.class);

    // Validation
    // Immediate fields
    assertAll(
        () -> assertNotNullAndLog(adminFactory.getHealthChecks(), prefix + ".healthChecks"),
        () -> assertNotNullAndLog(adminFactory.getTasks(), prefix + ".tasks"));
    assertAllFieldsUsedAndClear();
    // Drill into nested objects
    testContents(adminFactory.getHealthChecks());
    testContents(adminFactory.getTasks());
  }

  private void testContents(@NonNull HealthCheckConfiguration healthChecks) {
    // Setup
    String prefix = "healthChecks";
    expectClassFieldsTested(prefix, HealthCheckConfiguration.class);

    // Validation
    // Immediate fields
    assertAll(
        () -> assertEqualsAndLog(1, healthChecks.getMinThreads(), prefix + ".minThreads"),
        () -> assertEqualsAndLog(4, healthChecks.getMaxThreads(), prefix + ".maxThreads"),
        () -> assertEqualsAndLog(1, healthChecks.getWorkQueueSize(), prefix + ".workQueueSize"),
        () -> assertTrueAndLog(healthChecks.isServletEnabled(), prefix + ".servletEnabled"));
    assertAllFieldsUsedAndClear();
    // No nested objects to check.
  }

  private void testContents(@NonNull TaskConfiguration tasks) {
    // Setup
    String prefix = "tasks";
    expectClassFieldsTested(prefix, TaskConfiguration.class);

    // Validation
    // Immediate fields
    assertAll(
        () ->
            assertFalseAndLog(
                tasks.isPrintStackTraceOnError(), prefix + ".printStackTraceOnError"));
    assertAllFieldsUsedAndClear();
    // No nested objects to check.
  }

  public void testContents(@NonNull LoggingFactory logging) {
    // Setup
    String prefix = "logging";
    expectClassFieldsTested(prefix, DefaultLoggingFactory.class);

    // Validation
    assertTrue(
        logging instanceof DefaultLoggingFactory, "logging instanceof DefaultLoggingFactory");
    DefaultLoggingFactory defaultLoggingFactory = (DefaultLoggingFactory) logging;
    // Immediate field validations
    assertAll(
        () -> assertEqualsAndLog("INFO", defaultLoggingFactory.getLevel(), prefix + ".level"),
        () -> assertNotNullAndLog(defaultLoggingFactory.getAppenders(), prefix + ".appenders"),
        () -> assertNotNullAndLog(defaultLoggingFactory.getLoggers(), prefix + ".loggers"));
    assertAllFieldsUsedAndClear();
    // Drill into nested objects
    testContents(defaultLoggingFactory.getAppenders());
    testContents(defaultLoggingFactory.getLoggers());
  }

  private void testContents(@NonNull Map<String, JsonNode> loggers) {
    // Setup
    String prefix = "codingchica";
    JsonNode codingChicaLogger = loggers.get(prefix);
    expectedFieldNames.addAll(loggers.keySet().stream().toList());
    expectedFieldNames.addAll(loggers.keySet().stream().map(key -> key + ".level").toList());

    // Validation
    assertEquals(1, loggers.size(), "loggers.size");
    // Immediate fields
    assertNotNullAndLog(codingChicaLogger, prefix);
    assertEqualsAndLog("INFO", codingChicaLogger.asText(), prefix + ".level");
    assertAllFieldsUsedAndClear();
    // No nested objects to validate
  }

  private void testContents(@NonNull List<AppenderFactory<ILoggingEvent>> appenders) {
    // Setup
    String prefix = "consoleAppenderFactory";
    expectClassFieldsTested(prefix, ConsoleAppenderFactory.class);

    // Validation
    assertEquals(1, appenders.size(), "appenders.size");
    AppenderFactory<ILoggingEvent> appenderFactory = appenders.get(0);
    assertNotNull(appenders, "appenderFactory");
    assertTrue(
        appenderFactory instanceof ConsoleAppenderFactory,
        "appenderFactory instanceof ConsoleAppenderFactory");
    ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory =
        (ConsoleAppenderFactory) appenderFactory;
    // Immediate fields
    assertAll(
        () ->
            assertEqualsAndLog(
                -1,
                consoleAppenderFactory.getDiscardingThreshold(),
                prefix + ".discardingThreshold"),
        () ->
            assertEqualsAndLog(
                new ArrayList<>(),
                consoleAppenderFactory.getFilterFactories(),
                prefix + ".filterFactories"),
        () ->
            assertFalseAndLog(
                consoleAppenderFactory.isIncludeCallerData(), prefix + ".includeCallerData"),
        () -> assertNullAndLog(consoleAppenderFactory.getLayout(), prefix + ".layout"),
        () -> assertNullAndLog(consoleAppenderFactory.getLogFormat(), prefix + ".logFormat"),
        () -> assertNullAndLog(consoleAppenderFactory.getMessageRate(), prefix + ".messageRate"),
        () -> assertEqualsAndLog(256, consoleAppenderFactory.getQueueSize(), prefix + ".queueSize"),
        () ->
            assertEqualsAndLog(
                ConsoleAppenderFactory.ConsoleStream.STDOUT,
                consoleAppenderFactory.getTarget(),
                prefix + ".target"),
        () ->
            assertEqualsAndLog("ALL", consoleAppenderFactory.getThreshold(), prefix + ".threshold"),
        () ->
            assertEqualsAndLog(
                TimeZone.getTimeZone("UTC"),
                consoleAppenderFactory.getTimeZone(),
                prefix + ".timeZone"));
    assertAllFieldsUsedAndClear();
    // No nested objects to validate
  }

  public void testContents(ServerFactory serverFactory) {
    // Setup
    String prefix = "defaultServerFactory";
    expectClassFieldsTested(prefix, DefaultServerFactory.class);
    Set<String> allowedMethods = new HashSet<>();
    allowedMethods.add("HEAD");
    allowedMethods.add("DELETE");
    allowedMethods.add("POST");
    allowedMethods.add("GET");
    allowedMethods.add("OPTIONS");
    allowedMethods.add("PUT");
    allowedMethods.add("PATCH");

    // Validation
    assertNotNull(serverFactory, "serverFactory");
    assertTrue(
        serverFactory instanceof DefaultServerFactory,
        "serverFactory instanceof DefaultServerFactory");

    DefaultServerFactory defaultServerFactory = (DefaultServerFactory) serverFactory;
    assertAll(
        () ->
            assertNotNullAndLog(
                defaultServerFactory.getAdminConnectors(), prefix + ".adminConnectors"),
        () ->
            assertEqualsAndLog(
                "/", defaultServerFactory.getAdminContextPath(), prefix + ".adminContextPath"),
        () ->
            assertEqualsAndLog(
                64, defaultServerFactory.getAdminMaxThreads(), prefix + ".adminMaxThreads"),
        () ->
            assertEqualsAndLog(
                1, defaultServerFactory.getAdminMinThreads(), prefix + ".adminMinThreads"),
        () ->
            assertEqualsAndLog(
                allowedMethods,
                defaultServerFactory.getAllowedMethods(),
                prefix + ".allowedMethods"),
        () ->
            assertNotNullAndLog(
                defaultServerFactory.getApplicationConnectors(), prefix + ".applicationConnectors"),
        () ->
            assertEqualsAndLog(
                "/",
                defaultServerFactory.getApplicationContextPath(),
                prefix + ".applicationContextPath"),
        () ->
            assertFalseAndLog(
                defaultServerFactory.getDetailedJsonProcessingExceptionMapper(),
                prefix + ".detailedJsonProcessingExceptionMapper"),
        () ->
            assertFalseAndLog(defaultServerFactory.getDumpAfterStart(), prefix + ".dumpAfterStart"),
        () ->
            assertFalseAndLog(defaultServerFactory.getDumpBeforeStop(), prefix + ".dumpBeforeStop"),
        () ->
            assertFalseAndLog(
                defaultServerFactory.isEnableAdminVirtualThreads(),
                prefix + ".enableAdminVirtualThreads"),
        () ->
            assertTrueAndLog(
                defaultServerFactory.getEnableThreadNameFilter(),
                prefix + ".enableThreadNameFilter"),
        () ->
            assertFalseAndLog(
                defaultServerFactory.isEnableVirtualThreads(), prefix + ".enableVirtualThreads"),
        () -> assertNullAndLog(defaultServerFactory.getGid(), prefix + ".gid"),
        () -> assertNullAndLog(defaultServerFactory.getGroup(), prefix + ".group"),
        () ->
            assertNotNullAndLog(
                defaultServerFactory.getGzipFilterFactory(), prefix + ".gzipFilterFactory"),
        () ->
            assertEqualsAndLog(
                io.dropwizard.util.Duration.minutes(1),
                defaultServerFactory.getIdleThreadTimeout(),
                prefix + ".idleThreadTimeout"),
        () ->
            assertTrueAndLog(
                defaultServerFactory.getJerseyRootPath().isEmpty(), prefix + ".jerseyRootPath"),
        () ->
            assertEqualsAndLog(
                1024, defaultServerFactory.getMaxQueuedRequests(), prefix + ".maxQueuedRequests"),
        () ->
            assertEqualsAndLog(1024, defaultServerFactory.getMaxThreads(), prefix + ".maxThreads"),
        () -> assertNullAndLog(defaultServerFactory.getMetricPrefix(), prefix + ".metricPrefix"),
        () -> assertEqualsAndLog(8, defaultServerFactory.getMinThreads(), prefix + ".minThreads"),
        () ->
            assertNullAndLog(
                defaultServerFactory.getNofileHardLimit(), prefix + ".nofileHardLimit"),
        () ->
            assertNullAndLog(
                defaultServerFactory.getNofileSoftLimit(), prefix + ".nofileSoftLimit"),
        () ->
            assertTrueAndLog(
                defaultServerFactory.getRegisterDefaultExceptionMappers(),
                prefix + ".registerDefaultExceptionMappers"),
        () ->
            assertNotNullAndLog(
                defaultServerFactory.getRequestLogFactory(), prefix + ".requestLogFactory"),
        () ->
            assertEqualsAndLog(
                ResponseMeteredLevel.COARSE,
                defaultServerFactory.getResponseMeteredLevel(),
                prefix + ".responseMeteredLevel"),
        () -> assertNotNullAndLog(defaultServerFactory.getServerPush(), prefix + ".serverPush"),
        () ->
            assertEqualsAndLog(
                io.dropwizard.util.Duration.seconds(30),
                defaultServerFactory.getShutdownGracePeriod(),
                prefix + ".shutdownGracePeriod"),
        () -> assertNullAndLog(defaultServerFactory.getStartsAsRoot(), prefix + ".startsAsRoot"),
        () ->
            assertTrueAndLog(
                defaultServerFactory.isThreadPoolSizedCorrectly(),
                prefix + ".threadPoolSizedCorrectly"),
        () -> assertNullAndLog(defaultServerFactory.getUid(), prefix + ".uid"),
        () -> assertNullAndLog(defaultServerFactory.getUmask(), prefix + ".umask"),
        () -> assertNullAndLog(defaultServerFactory.getUser(), prefix + ".user"));

    assertAllFieldsUsedAndClear();
    // Drill into nested objects.
    validateContents_server_adminConnectors(defaultServerFactory.getAdminConnectors());
    validateContents_server_applicationConnectors(defaultServerFactory.getApplicationConnectors());
    validateContents_server_gzip(defaultServerFactory.getGzipFilterFactory());
    validateContents_server_requestLogFactory(defaultServerFactory.getRequestLogFactory());
    validateContents_server_serverPush(defaultServerFactory.getServerPush());
  }

  /**
   * Ensure that the <em>applicationConnectors</em> section of the configuration is setup as
   * expected.
   *
   * @param applicationConnectors The object to validate.
   * @see <a href="https://www.dropwizard.io/en/latest/manual/configuration.html#http">Dropwizard
   *     Config Reference: HTTP connector</a>
   */
  private void validateContents_server_applicationConnectors(
      List<ConnectorFactory> applicationConnectors) {
    if (isTestConfig) {
      validateContents_server_httpConnectorFactory(0, applicationConnectors);
    } else {
      validateContents_server_httpConnectorFactory(8080, applicationConnectors);
    }
  }

  /**
   * Ensure that the <em>applicationConnectors</em> section of the configuration is setup as
   * expected.
   *
   * @param applicationConnectors The object to validate.
   * @see <a href="https://www.dropwizard.io/en/latest/manual/configuration.html#http">Dropwizard
   *     Config Reference: HTTP connector</a>
   */
  private void validateContents_server_adminConnectors(
      List<ConnectorFactory> applicationConnectors) {
    if (isTestConfig) {
      validateContents_server_httpConnectorFactory(0, applicationConnectors);
    } else {
      validateContents_server_httpConnectorFactory(8081, applicationConnectors);
    }
  }

  /**
   * Ensure that the <em>applicationConnectors</em> section of the configuration is setup as
   * expected.
   *
   * @param applicationConnectors The object to validate.
   * @see <a href="https://www.dropwizard.io/en/latest/manual/configuration.html#http">Dropwizard
   *     Config Reference: HTTP connector</a>
   */
  private void validateContents_server_httpConnectorFactory(
      int port, List<ConnectorFactory> applicationConnectors) {
    // Setup
    String prefix = "httpConnectorFactory";
    expectClassFieldsTested("applicationConnectors", ConnectorFactory.class);
    expectClassFieldsTested(prefix, HttpConnectorFactory.class);

    // Validations
    assertNotNull(applicationConnectors, "applicationConnectors");
    assertEquals(1, applicationConnectors.size(), "applicationConnectors.size");

    ConnectorFactory connectorFactory = applicationConnectors.get(0);
    assertNotNull(connectorFactory, "applicationConnectors.http");

    HttpConnectorFactory httpConnectorFactory = (HttpConnectorFactory) connectorFactory;
    // Immediate fields
    assertAll(
        () ->
            assertTrueAndLog(
                httpConnectorFactory.getAcceptorThreads().isEmpty(), prefix + ".acceptorThreads"),
        () ->
            assertNullAndLog(
                httpConnectorFactory.getAcceptQueueSize(), prefix + ".acceptQueueSize"),
        () -> assertNullAndLog(httpConnectorFactory.getBindHost(), prefix + ".bindHost"),
        () ->
            assertEqualsAndLog(
                DataSize.bytes(1024),
                httpConnectorFactory.getBufferPoolIncrement(),
                prefix + ".bufferPoolIncrement"),
        () ->
            assertEqualsAndLog(
                DataSize.bytes(512),
                httpConnectorFactory.getHeaderCacheSize(),
                prefix + ".headerCacheSize"),
        () ->
            assertEqualsAndLog(
                HttpCompliance.RFC7230,
                httpConnectorFactory.getHttpCompliance(),
                prefix + ".httpCompliance"),
        () ->
            assertEqualsAndLog(
                io.dropwizard.util.Duration.seconds(30),
                httpConnectorFactory.getIdleTimeout(),
                prefix + ".idleTimeout"),
        () ->
            assertFalseAndLog(httpConnectorFactory.isInheritChannel(), prefix + ".inheritChannel"),
        () ->
            assertEqualsAndLog(
                DataSize.kibibytes(8),
                httpConnectorFactory.getInputBufferSize(),
                prefix + ".inputBufferSize"),
        () ->
            assertEqualsAndLog(
                DataSize.kibibytes(64),
                httpConnectorFactory.getMaxBufferPoolSize(),
                prefix + ".maxBufferPoolSize"),
        () ->
            assertEqualsAndLog(
                DataSize.kibibytes(8),
                httpConnectorFactory.getMaxRequestHeaderSize(),
                prefix + ".maxRequestHeaderSize"),
        () ->
            assertEqualsAndLog(
                DataSize.kibibytes(8),
                httpConnectorFactory.getMaxResponseHeaderSize(),
                prefix + ".maxResponseHeaderSize"),
        () ->
            assertEqualsAndLog(
                DataSize.bytes(64),
                httpConnectorFactory.getMinBufferPoolSize(),
                prefix + ".minBufferPoolSize"),
        () ->
            assertEqualsAndLog(
                DataSize.bytes(0),
                httpConnectorFactory.getMinRequestDataPerSecond(),
                prefix + ".minRequestDataPerSecond"),
        () ->
            assertEqualsAndLog(
                DataSize.bytes(0),
                httpConnectorFactory.getMinResponseDataPerSecond(),
                prefix + ".minResponseDataPerSecond"),
        () ->
            assertEqualsAndLog(
                DataSize.kibibytes(32),
                httpConnectorFactory.getOutputBufferSize(),
                prefix + ".outputBufferSize"),
        () -> assertEqualsAndLog(port, httpConnectorFactory.getPort(), prefix + ".port"),
        () ->
            assertEqualsAndLog(
                CookieCompliance.RFC6265,
                httpConnectorFactory.getRequestCookieCompliance(),
                prefix + ".requestCookieCompliance"),
        () ->
            assertEqualsAndLog(
                CookieCompliance.RFC6265,
                httpConnectorFactory.getResponseCookieCompliance(),
                prefix + ".responseCookieCompliance"),
        () ->
            assertEqualsAndLog(
                true, httpConnectorFactory.isReuseAddress(), prefix + ".reuseAddress"),
        () ->
            assertTrueAndLog(
                httpConnectorFactory.getSelectorThreads().isEmpty(), prefix + ".selectorThreads"),
        () ->
            assertEqualsAndLog(
                UriCompliance.DEFAULT,
                httpConnectorFactory.getUriCompliance(),
                prefix + ".uriCompliance"),
        () -> assertTrueAndLog(httpConnectorFactory.isUseDateHeader(), prefix + ".useDateHeader"),
        () ->
            assertFalseAndLog(
                httpConnectorFactory.isUseForwardedHeaders(), prefix + ".useForwardedHeaders"),
        () ->
            assertFalseAndLog(
                httpConnectorFactory.isUseProxyProtocol(), prefix + ".useProxyProtocol"),
        () ->
            assertFalseAndLog(
                httpConnectorFactory.isUseServerHeader(), prefix + ".useServerHeader"));
    assertAllFieldsUsedAndClear();
    // Nested fields
  }

  /**
   * Ensure that the <em>applicationConnectors</em> section of the configuration is setup as
   * expected.
   *
   * @param serverPush The object to validate.
   * @see <a
   *     href="https://www.dropwizard.io/en/latest/manual/configuration.html#server-push">Dropwizard
   *     Config Reference: Server Push</a>
   */
  private void validateContents_server_serverPush(@NonNull ServerPushFilterFactory serverPush) {
    // Setup
    String prefix = "serverPush";
    expectClassFieldsTested("serverPush", ServerPushFilterFactory.class);

    // Validation
    // Immediate fields
    assertAll(
        () ->
            assertEqualsAndLog(
                io.dropwizard.util.Duration.seconds(4),
                serverPush.getAssociatePeriod(),
                prefix + ".associatePeriod"),
        () -> assertFalseAndLog(serverPush.isEnabled(), prefix + ".enabled"),
        () -> assertEqualsAndLog(16, serverPush.getMaxAssociations(), prefix + ".maxAssociations"),
        () -> assertNullAndLog(serverPush.getRefererHosts(), prefix + ".refererHosts"),
        () -> assertNullAndLog(serverPush.getRefererPorts(), prefix + ".refererPorts"));
    assertAllFieldsUsedAndClear();
    // No nested objects to validate.
  }

  /**
   * Ensure that the <em>request log</em> section of the configuration is setup as expected.
   *
   * @param requestLogFactory The object to validate.
   * @see <a
   *     href="https://www.dropwizard.io/en/latest/manual/configuration.html#request-log">Dropwizard
   *     Config Reference: Request Log</a>
   */
  private void validateContents_server_requestLogFactory(
      @NonNull RequestLogFactory<?> requestLogFactory) {
    // Setup
    String logbackPrefix = "logbackAccessRequestLogFactory";
    expectedFieldNames.add(logbackPrefix);
    expectClassFieldsTested(logbackPrefix, LogbackAccessRequestLogFactory.class);
    assertTrue(
        requestLogFactory instanceof LogbackAccessRequestLogFactory,
        "requestLogFactory instanceof LogbackAccessRequestLogFactory");

    // Execution
    LogbackAccessRequestLogFactory logbackAccessRequestLogFactory =
        (LogbackAccessRequestLogFactory) requestLogFactory;

    // Validation
    List<AppenderFactory<IAccessEvent>> appenders = logbackAccessRequestLogFactory.getAppenders();
    AppenderFactory<IAccessEvent> appenderFactory = appenders.get(0);
    assertNotNullAndLog(logbackAccessRequestLogFactory, logbackPrefix);
    assertAll(
        () ->
            assertTrueAndLog(
                logbackAccessRequestLogFactory.isEnabled(), logbackPrefix + ".enabled"),
        () -> assertNotNullAndLog(appenders, logbackPrefix + ".appenders"),
        () -> assertEqualsAndLog(1, appenders.size(), logbackPrefix + ".appenders"));
    assertTrue(
        appenderFactory instanceof ConsoleAppenderFactory,
        "appenderFactory instanceof ConsoleAppenderFactory");
    // Drill into nested objects
    testContents(logbackAccessRequestLogFactory);
  }

  private void testContents(
      @NonNull LogbackAccessRequestLogFactory logbackAccessRequestLogFactory) {
    // Setup
    String prefix = "logbackAccessRequestLogFactory";
    expectedFieldNames.add(prefix);
    expectClassFieldsTested(prefix, LogbackAccessRequestLogFactory.class);

    // Validation
    List<AppenderFactory<IAccessEvent>> appenders = logbackAccessRequestLogFactory.getAppenders();
    assertAll(
        () -> assertTrueAndLog(logbackAccessRequestLogFactory.isEnabled(), prefix + ".enabled"),
        () -> assertNotNullAndLog(appenders, prefix + ".appenders"),
        () -> assertEqualsAndLog(1, appenders.size(), prefix + ".appenders"));
    assertAllFieldsUsedAndClear();
    // Drill into nested objects
    testContents(appenders.get(0));
  }

  private void testContents(@NonNull AppenderFactory<IAccessEvent> appenderFactory) {
    // Setup
    String prefix = "logbackAccessRequestLogFactory";
    expectClassFieldsTested(prefix, AppenderFactory.class);

    // Validation
    assertTrue(
        appenderFactory instanceof ConsoleAppenderFactory,
        "appenderFactory instanceof ConsoleAppenderFactory");
    assertAllFieldsUsedAndClear();
    // Drill into nested objects
    testContents((ConsoleAppenderFactory<IAccessEvent>) appenderFactory);
  }

  private void testContents(@NonNull ConsoleAppenderFactory<IAccessEvent> consoleAppenderFactory) {
    // Setup
    String prefix = "consoleAppenderFactory";
    expectClassFieldsTested(prefix, ConsoleAppenderFactory.class);

    // Validation
    assertAll(
        () ->
            assertEqualsAndLog(
                -1,
                consoleAppenderFactory.getDiscardingThreshold(),
                prefix + ".discardingThreshold"),
        () ->
            assertEqualsAndLog(
                new ArrayList<>(),
                consoleAppenderFactory.getFilterFactories(),
                prefix + ".filterFactories"),
        () ->
            assertFalseAndLog(
                consoleAppenderFactory.isIncludeCallerData(), prefix + ".includeCallerData"),
        () -> assertNullAndLog(consoleAppenderFactory.getLogFormat(), prefix + ".logFormat"),
        () -> assertNullAndLog(consoleAppenderFactory.getLayout(), prefix + ".layout"),
        () -> assertNullAndLog(consoleAppenderFactory.getMessageRate(), prefix + ".messageRate"),
        () ->
            assertEqualsAndLog(
                ConsoleAppenderFactory.ConsoleStream.STDOUT,
                consoleAppenderFactory.getTarget(),
                prefix + ".target"),
        () ->
            assertEqualsAndLog("ALL", consoleAppenderFactory.getThreshold(), prefix + ".threshold"),
        () ->
            assertEqualsAndLog(
                TimeZone.getTimeZone("UTC"),
                consoleAppenderFactory.getTimeZone(),
                prefix + ".timeZone"),
        () ->
            assertEqualsAndLog(256, consoleAppenderFactory.getQueueSize(), prefix + ".queueSize"));
    assertAllFieldsUsedAndClear();
    // No nested objects to validate.
  }

  /**
   * Ensure that the <em>applicationConnectors</em> section of the configuration is setup as
   * expected.
   *
   * @param gzipFilterFactory The object to validate.
   * @see <a href="https://www.dropwizard.io/en/latest/manual/configuration.html#gzip">Dropwizard
   *     Config Reference: GZip</a>
   */
  private void validateContents_server_gzip(@NonNull GzipHandlerFactory gzipFilterFactory) {
    // Setup
    String prefix = "gzipFilterFactory";
    expectClassFieldsTested(prefix, GzipHandlerFactory.class);

    // Validation
    // Individual fields
    assertAll(
        () ->
            assertEqualsAndLog(
                DataSize.kibibytes(8), gzipFilterFactory.getBufferSize(), prefix + ".bufferSize"),
        () ->
            assertNullAndLog(
                gzipFilterFactory.getCompressedMimeTypes(), prefix + ".compressedMimeTypes"),
        () ->
            assertEqualsAndLog(
                -1,
                gzipFilterFactory.getDeflateCompressionLevel(),
                prefix + ".deflateCompressionLevel"),
        () -> assertTrueAndLog(gzipFilterFactory.isEnabled(), prefix + ".enabled"),
        () ->
            assertNullAndLog(
                gzipFilterFactory.getExcludedMimeTypes(), prefix + ".excludedMimeTypes"),
        () -> assertNullAndLog(gzipFilterFactory.getExcludedPaths(), prefix + ".excludedPaths"),
        () -> assertNullAndLog(gzipFilterFactory.getIncludedMethods(), prefix + ".includedMethods"),
        () -> assertNullAndLog(gzipFilterFactory.getIncludedPaths(), prefix + ".includedPaths"),
        () ->
            assertEqualsAndLog(
                DataSize.bytes(256),
                gzipFilterFactory.getMinimumEntitySize(),
                prefix + ".minimumEntitySize"),
        () -> assertFalseAndLog(gzipFilterFactory.isSyncFlush(), prefix + ".syncFlush"));
    assertAllFieldsUsedAndClear();
    // No objects to drill into.
  }

  private void assertEqualsAndLog(Object expectedValue, Object actualValue, String fieldName) {
    assertEquals(expectedValue, actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertEqualsAndLog(int expectedValue, int actualValue, String fieldName) {
    assertEquals(expectedValue, actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertNullAndLog(Object actualValue, String fieldName) {
    assertNull(actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertNotNullAndLog(Object actualValue, String fieldName) {
    assertNotNull(actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertFalseAndLog(boolean actualValue, String fieldName) {
    assertFalse(actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertTrueAndLog(boolean actualValue, String fieldName) {
    assertTrue(actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private <T> void expectClassFieldsTested(String prefix, Class<T> clazz) {
    Set<String> fieldNames =
        Arrays.stream(clazz.getMethods())
            .filter(method -> !"getClass".equals(method.getName()))
            .filter(method -> !"get".equals(method.getName()))
            .filter(method -> !void.class.equals(method.getReturnType()))
            .filter(
                method ->
                    StringUtils.startsWith(method.getName(), "get")
                        || StringUtils.startsWith(method.getName(), "is"))
            .map(
                (method) -> {
                  String fieldName = StringUtils.removeStart(method.getName(), "is");
                  fieldName = StringUtils.removeStart(fieldName, "get");
                  String firstChar = StringUtils.left(fieldName, 1);
                  firstChar = StringUtils.lowerCase(firstChar);
                  fieldName = StringUtils.substring(fieldName, 1);
                  fieldName = firstChar + fieldName;
                  return prefix + "." + fieldName;
                })
            .collect(Collectors.toCollection(LinkedHashSet::new));
    expectedFieldNames.addAll(fieldNames);
  }

  private void assertAllFieldsUsedAndClear() {
    List<String> expectedFieldNamesList = expectedFieldNames.stream().sorted().toList();
    List<String> actualFieldNamesList = testedFields.stream().sorted().toList();
    int currentIndex = 0;
    for (int i = 0; i < expectedFieldNamesList.size() && i < actualFieldNamesList.size(); i++) {
      currentIndex++;
      assertEquals(
          expectedFieldNamesList.get(i),
          actualFieldNamesList.get(i),
          String.format(
              "%s: enforce all fields tested: Expected:%n%s%nvs. Actual:%n%s",
              i, expectedFieldNamesList, actualFieldNamesList));
    }
    if (expectedFieldNamesList.size() > actualFieldNamesList.size()) {
      StringBuffer buffer = new StringBuffer("Expected fields not tested: ");
      for (int i = currentIndex; i < expectedFieldNamesList.size(); i++) {
        if (i > currentIndex) {
          buffer.append(", ");
        }
        buffer.append(expectedFieldNamesList.get(i));
      }
      buffer.append(
          String.format("%n%s%nvs. Actual%n%s", expectedFieldNamesList, actualFieldNamesList));
      fail(buffer.toString());
    }
    if (expectedFieldNamesList.size() < actualFieldNamesList.size()) {
      StringBuffer buffer = new StringBuffer("Unexpected fields tested: ");
      for (int i = currentIndex; i < actualFieldNamesList.size(); i++) {
        if (i > currentIndex) {
          buffer.append(", ");
        }
        buffer.append(actualFieldNamesList.get(i));
      }
      buffer.append(
          String.format("%n%s%nvs. Actual%n%s", expectedFieldNamesList, actualFieldNamesList));
      fail(buffer.toString());
    }
    expectedFieldNames.clear();
    testedFields.clear();
  }
}
