package com.codingchica.flashcards.component.steps.api;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.FlashCardsApplication;
import com.codingchica.flashcards.component.model.APICallWorld;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.core.options.CurlOption;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/** Cucumber steps that can be used for generic API calls to the Dropwizard server. */
@Slf4j
public class GenericAPISteps {
  private ObjectMapper objectMapper = new ObjectMapper();
  private static DropwizardAppExtension<FlashCardsConfiguration> DROP_WIZARD_SERVER =
      new DropwizardAppExtension<>(
          FlashCardsApplication.class,
          ResourceHelpers.resourceFilePath("appConfig/test-component.yml"));

  /**
   * State storage between steps. A separate copy will be created for each test / scenario, but not
   * each step.
   */
  private final APICallWorld world = new APICallWorld();

  /**
   * A custom step to map a port name to a Dropwizard API port, in case we support dynamic ports in
   * the future.
   *
   * @param portName The name of the port as indicated in the feature file. One of
   *     admin|application.
   * @return The corresponding port from the Dropwizard server in use for the component testing.
   */
  @ParameterType("admin|application")
  public int portName(String portName) {
    return switch (portName) {
      case "admin" -> DROP_WIZARD_SERVER.getAdminPort();
      case "application" -> DROP_WIZARD_SERVER.getLocalPort();
      default -> throw new IllegalArgumentException("Unexpected portName: " + portName);
    };
  }

  @BeforeAll
  public static void setup() throws Exception {
    DROP_WIZARD_SERVER.before();
  }

  @AfterAll
  public static void teardown() {
    DROP_WIZARD_SERVER.after();
  }

  @Given("that my request uses the {word} protocol")
  public void that_my_request_uses_the_protocol(String protocol) {
    world.protocol = protocol;
  }

  @Given("that my request goes to endpoint {string}")
  @Given("that my request goes to endpoint {word}")
  public void setPath(String path) {
    world.path = replaceKeywords(path);
  }

  private String replaceKeywords(String value) {
    String valueToUse = value;
    if (world.id != null) {
      valueToUse = StringUtils.replaceIgnoreCase(valueToUse, "{ID}", world.id.toString());
    }
    if (world.newId == null) {
      world.newId = UUID.randomUUID();
    }
    valueToUse = StringUtils.replaceIgnoreCase(valueToUse, "{NEW_ID}", world.newId.toString());
    return valueToUse;
  }

  @Given("that my request goes to the {portName} port")
  public void setPort(int portName) {
    world.port = portName;
  }

  @Given("that my request is for a valid quiz ID")
  public void getQuizId() throws URISyntaxException, IOException {
    objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);

    String endpoint =
        String.format(
            "%s://%s:%s/%s", world.protocol, world.server, world.port, "quizzes/Adding%200");
    log.debug(endpoint);
    URL url = new URI(endpoint).toURL();
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setDoOutput(true);

    // Execution
    connection.connect();
    String responseBody = getResponseBody(connection);
    log.debug(responseBody);

    DocumentContext jsonBody = JsonPath.parse(responseBody);

    world.quiz = objectMapper.readValue(responseBody, Quiz.class);
    world.id = UUID.fromString(jsonBody.read("id").toString());
  }

  @And("that my request body is for quiz {string} with {int} correct answers")
  public void thatMyRequestBodyIsForQuizAddingWithCorrectAnswersCountCorrectAnswers(
      String quizName, int correctAnswerCount) throws JsonProcessingException {
    Preconditions.checkNotNull(world.quiz, "world.quiz must not be null");
    List<String> answers = new ArrayList<>();
    int countOfCorrectAnswers = 0;
    for (int i = 0; i < world.quiz.getPrompts().size(); i++) {
      Map.Entry<String, String> prompt = world.quiz.getPrompts().get(i);
      if (countOfCorrectAnswers < correctAnswerCount) {
        answers.add(prompt.getValue());
        countOfCorrectAnswers++;
      } else {
        answers.add(prompt.getValue() + "something extra");
      }
    }
    CompletedQuiz completedQuiz = CompletedQuiz.builder().name(quizName).answers(answers).build();
    world.requestBody = objectMapper.writeValueAsString(completedQuiz);
  }

  @Given("that my request contains header {word} = {word}")
  public void setContentTypeRequestHeader(String header, String value) {
    world.requestHeaders.put(header, value);
  }

  @Given("that my request uses the {word} method")
  public void setHttpMethod(String methodName) {
    world.httpMethod = CurlOption.HttpMethod.valueOf(methodName);
  }

  @Given("that my request body is {string}")
  public void setRequestBody(String body) {
    world.requestBody = body;
  }

  @When("I submit the request")
  public void sendRequest() throws IOException, URISyntaxException {
    // Setup
    world.endpoint =
        String.format("%s://%s:%s/%s", world.protocol, world.server, world.port, world.path);
    world.url = new URI(world.endpoint).toURL();
    log.debug("Calling {} - {}", world.httpMethod.name(), world.url);
    world.connection = (HttpURLConnection) world.url.openConnection();
    log.debug("Request headers: ");
    world.requestHeaders.forEach(
        (header, value) -> {
          if (StringUtils.containsIgnoreCase(header, "Auth")) {
            log.debug("   {} = <omitted>", header);
          } else {
            log.debug("   {} = {}", header, value);
          }
          world.connection.setRequestProperty(header, value);
        });
    world.connection.setRequestMethod(world.httpMethod.name());
    world.connection.setDoOutput(true);
    if (world.requestBody != null) {
      try (final OutputStream outputStream = world.connection.getOutputStream();
          final OutputStreamWriter outputStreamWriter =
              new OutputStreamWriter(outputStream, StandardCharsets.UTF_8.name())) {
        outputStreamWriter.write(world.requestBody);
        outputStreamWriter.flush();
        log.debug(world.requestBody);
      }
    }

    // Execution
    world.connection.connect();
  }

  private String getResponseEntity() throws IOException {
    if (world.responseBody == null) {
      if (getResponseBody() == null) {
        getResponseError();
      }
    }
    return world.responseBody;
  }

  private String getResponseBody(HttpURLConnection connection) throws IOException {
    String responseReceived = null;
    InputStream responseBodyStream = null;
    try {
      responseBodyStream = connection.getInputStream();
    } catch (IOException e) {
      log.debug("Exception while retrieving response body: " + e.getMessage());
    }
    if (responseBodyStream != null) {
      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(responseBodyStream, StandardCharsets.UTF_8))) {
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());
        }
        responseReceived = response.toString();
      }
    }
    return responseReceived;
  }

  private String getResponseBody() throws IOException {
    String responseBody = getResponseBody(world.connection);

    world.responseBody = responseBody;
    return responseBody;
  }

  private String getResponseError(HttpURLConnection connection) throws IOException {
    String responseReceived = null;
    InputStream errorStream = connection.getErrorStream();
    if (errorStream != null) {
      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());
        }
        responseReceived = response.toString();
      }
    }
    return responseReceived;
  }

  private String getResponseError() throws IOException {
    String responseBody = getResponseError(world.connection);
    world.responseBody = responseBody;
    return responseBody;
  }

  @Then("the response code is {int}")
  public void theResponseCodeMatches(int expectedResponseCode) throws IOException {
    // Validation
    String actualResponseBody = getResponseEntity();
    assertEquals(
        expectedResponseCode,
        world.connection.getResponseCode(),
        "http status code mismatch calling " + world.endpoint + ": " + actualResponseBody);
  }

  @Then("the response body is {string}")
  @Then("the response body is {word}")
  public void theResponseBodyEquals(String responseBody) throws IOException {
    String actualResponseBody = getResponseEntity();
    assertNotNull(
        actualResponseBody,
        "Expected response body to not be null, but it was.  Here is the error body: "
            + actualResponseBody);
    assertEquals(responseBody, actualResponseBody, "mismatch mismatch calling " + world.endpoint);
  }

  @Then("the response body contains UUID at path\\(s)")
  public void theResponseBodyContainsUUIDPaths(List<String> paths) throws IOException {
    String responseBody = getResponseEntity();
    assertNotNull(
        responseBody,
        "Expected response body to not be null, but it was.  Here is the error body: "
            + responseBody);
    DocumentContext jsonBody = JsonPath.parse(responseBody);
    assertNotNull(paths, "paths");
    assertNotNull(responseBody, "responseBody");
    paths.forEach(
        (path) -> {
          try {
            assertNotNull(
                UUID.fromString(jsonBody.read(path).toString()),
                "Mismatch on '" + path + "' in response = " + responseBody);
          } catch (Throwable t) {
            log.debug("Actual Response: " + responseBody);
            throw t;
          }
        });
  }

  @Then("the response body contains Instant at path\\(s)")
  public void theResponseBodyContainsInstantPaths(List<String> dateTimePaths) throws IOException {
    String responseBody = getResponseEntity();
    assertNotNull(
        responseBody,
        "Expected response body to not be null, but it was.  Here is the error body: "
            + responseBody);
    DocumentContext jsonBody = JsonPath.parse(responseBody);
    assertNotNull(dateTimePaths, "expectedResponseData");
    assertNotNull(responseBody, "responseBody");
    dateTimePaths.forEach(
        (path) -> {
          try {
            assertNotNull(
                Instant.parse(jsonBody.read(path).toString()),
                "Mismatch on '" + path + "' in response = " + responseBody);
          } catch (Throwable t) {
            log.debug("Actual Response: " + responseBody);
            throw t;
          }
        });
  }

  @Then("the response body contains JSON data")
  public void theResponseBodyMatchesPattern(Map<String, String> expectedResponseData)
      throws IOException {
    String responseBody = getResponseEntity();
    assertNotNull(
        responseBody,
        "Expected response body to not be null, but it was.  Here is the error body: "
            + responseBody);
    DocumentContext jsonBody = JsonPath.parse(responseBody);
    assertNotNull(expectedResponseData, "expectedResponseData");
    assertNotNull(responseBody, "responseBody");
    expectedResponseData.forEach(
        (path, value) -> {
          try {
            assertEquals(
                replaceKeywords(value),
                jsonBody.read(path).toString(),
                "Mismatch on '" + path + "' in response = " + responseBody);
          } catch (Throwable t) {
            log.debug("Actual Response: " + responseBody);
            throw t;
          }
        });
  }

  @Then("the error response body contains JSON data")
  public void theResponseErrorMatchesPattern(Map<String, String> expectedResponseData)
      throws IOException {
    String responseBody = getResponseEntity();
    assertNotNull(
        responseBody,
        "Expected response error to not be null, but it was.  Here is the regular body: "
            + responseBody);
    DocumentContext jsonBody = JsonPath.parse(responseBody);
    assertNotNull(expectedResponseData, "expectedResponseData");
    assertNotNull(responseBody, "responseError");
    expectedResponseData.forEach(
        (path, value) -> {
          try {
            assertEquals(
                replaceKeywords(value),
                jsonBody.read(path).toString(),
                "Mismatch on '" + path + "' in response = " + responseBody);
          } catch (Throwable t) {
            log.debug("Response: " + responseBody);
            throw t;
          }
        });
  }

  @Then("the response body contains String data")
  public void theResponseBodyMatchesPattern(List<String> expectedResponseSnippets)
      throws IOException {
    String responseBody = getResponseEntity();
    assertNotNull(expectedResponseSnippets, "expectedResponseSnippets");
    assertNotNull(responseBody, "responseBody");
    expectedResponseSnippets.forEach(
        (value) -> {
          assertTrue(
              StringUtils.contains(responseBody, value),
              "No hit for '" + value + "' in response:\n" + responseBody);
        });
  }

  @Then("the response body is completely empty")
  public void theResponseBodyIsNotSet() throws IOException {
    assertEquals("", getResponseEntity(), "mismatch mismatch calling " + world.endpoint);
  }
}
