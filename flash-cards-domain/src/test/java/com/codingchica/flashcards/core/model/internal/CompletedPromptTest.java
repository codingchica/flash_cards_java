package com.codingchica.flashcards.core.model.internal;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.util.AnnotationValidationUtils;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class CompletedPromptTest {

  /**
   * A collection to allow tests that should include validation every field to enforce that new
   * fields are also added.
   */
  private final Set<String> testedFields = new TreeSet<>();

  private final Set<String> expectedFieldNames = new TreeSet<>();

  private CompletedPrompt.Builder completedPromptBuilder = CompletedPrompt.builder();

  private CompletedPrompt completedPrompt =
      completedPromptBuilder
          .prompt("prompt")
          .expectedAnswer("expected")
          .answerProvided("answer")
          .build();

  @Nested
  class POJOTest {

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class PromptTest {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY prompt GoEs HeRE", " "})
      void testGetterViaBuilder(String value) {
        // Setup
        completedPrompt = completedPromptBuilder.prompt(value).build();

        // Execution
        String result = completedPrompt.getPrompt();

        // Validation
        assertEquals(value, result);
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY prompt GoEs HeRE", " "})
      void testGetterViaSetter(String value) {
        // Setup
        completedPrompt.setPrompt(value);

        // Execution
        String result = completedPrompt.getPrompt();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class ExpectedAnswerTest {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY value GoEs HeRE", " "})
      void testGetterViaBuilder(String value) {
        // Setup
        completedPrompt = completedPromptBuilder.expectedAnswer(value).build();

        // Execution
        String result = completedPrompt.getExpectedAnswer();

        // Validation
        assertEquals(value, result);
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY value GoEs HeRE", " "})
      void testGetterViaSetter(String value) {
        // Setup
        completedPrompt.setExpectedAnswer(value);

        // Execution
        String result = completedPrompt.getExpectedAnswer();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class AnswerProvidedTest {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY value GoEs HeRE", " "})
      void testGetterViaBuilder(String value) {
        // Setup
        completedPrompt = completedPromptBuilder.answerProvided(value).build();

        // Execution
        String result = completedPrompt.getAnswerProvided();

        // Validation
        assertEquals(value, result);
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY value GoEs HeRE", " "})
      void testGetterViaSetter(String value) {
        // Setup
        completedPrompt.setAnswerProvided(value);

        // Execution
        String result = completedPrompt.getAnswerProvided();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class CorrectAnswerTest {

      @ParameterizedTest
      @ValueSource(booleans = {true, false})
      void testGetterViaBuilder(boolean value) {
        // Setup
        completedPrompt = completedPromptBuilder.correctAnswer(value).build();

        // Execution
        boolean result = completedPrompt.isCorrectAnswer();

        // Validation
        assertEquals(value, result);
      }

      @ParameterizedTest
      @ValueSource(booleans = {true, false})
      void testGetterViaSetter(boolean value) {
        // Setup
        completedPrompt.setCorrectAnswer(value);

        // Execution
        boolean result = completedPrompt.isCorrectAnswer();

        // Validation
        assertEquals(value, result);
      }
    }

    @Nested
    class NoArgConstructorTest {
      @Test
      void whenInvoked_thenExpectedObjectReturned() {
        // Execution
        CompletedPrompt completedPrompt = new CompletedPrompt();
        String prefix = "completedPrompt";
        expectClassFieldsTested(prefix, CompletedPrompt.class);

        // Validation
        assertNotNull(completedPrompt);
        assertAll(
            () -> assertNullAndLog(completedPrompt.getPrompt(), prefix + ".prompt"),
            () -> assertFalseAndLog(completedPrompt.isCorrectAnswer(), prefix + ".correctAnswer"),
            () -> assertNullAndLog(completedPrompt.getExpectedAnswer(), prefix + ".expectedAnswer"),
            () -> assertNullAndLog(completedPrompt.getAnswerProvided(), prefix + ".answerProvided"),
            CompletedPromptTest.this::assertAllFieldsUsedAndClear);
      }
    }
  }

  @Nested
  class ValidationTest {

    private final Validator validator = BaseValidator.newValidator();

    /** Generic happy-path scenario */
    @Test
    void happyPath() {
      // Setup
      // See ConfigFactory for setup.

      // Execution
      final Set<ConstraintViolation<CompletedPrompt>> violations =
          validator.validate(completedPrompt);

      // Validation
      AnnotationValidationUtils.assertEmpty(violations);
    }

    @Nested
    class PromptTest {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(
          strings = {
            " ",
          })
      void whenBlank_thenNotValid(String value) {
        // Setup
        completedPrompt = completedPromptBuilder.prompt(value).build();

        // Execution
        final Set<ConstraintViolation<CompletedPrompt>> violations =
            validator.validate(completedPrompt);

        // Validation
        AnnotationValidationUtils.assertOneViolation("prompt must not be blank", violations);
      }
    }

    @Nested
    class ExpectedAnswerTest {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(
          strings = {
            " ",
          })
      void whenBlank_thenNotValid(String value) {
        // Setup
        completedPrompt = completedPromptBuilder.expectedAnswer(value).build();

        // Execution
        final Set<ConstraintViolation<CompletedPrompt>> violations =
            validator.validate(completedPrompt);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "expectedAnswer must not be blank", violations);
      }
    }

    @Nested
    class AnswerProvidedTest {
      @ParameterizedTest
      @NullSource
      void whenNull_thenNotValid(String value) {
        // Setup
        completedPrompt = completedPromptBuilder.answerProvided(value).build();

        // Execution
        final Set<ConstraintViolation<CompletedPrompt>> violations =
            validator.validate(completedPrompt);

        // Validation
        AnnotationValidationUtils.assertOneViolation("answerProvided must not be null", violations);
      }

      @ParameterizedTest
      @EmptySource
      @ValueSource(
          strings = {
            " ",
          })
      void whenBlank_thenValid(String value) {
        // Setup
        completedPrompt = completedPromptBuilder.answerProvided(value).build();

        // Execution
        final Set<ConstraintViolation<CompletedPrompt>> violations =
            validator.validate(completedPrompt);

        // Validation
        AnnotationValidationUtils.assertEmpty(violations);
      }
    }
  }

  private void assertNullAndLog(Object actualValue, String fieldName) {
    assertNull(actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertFalseAndLog(boolean actualValue, String fieldName) {
    assertFalse(actualValue, fieldName);
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
