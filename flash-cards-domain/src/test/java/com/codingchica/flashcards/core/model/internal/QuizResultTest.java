package com.codingchica.flashcards.core.model.internal;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.util.AnnotationValidationUtils;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class QuizResultTest {

  /**
   * A collection to allow tests that should include validation every field to enforce that new
   * fields are also added.
   */
  private Set<String> testedFields = new TreeSet<>();

  private QuizResult.Builder quizResultBuilder =
      QuizResult.builder().createdDateTime(Instant.now()).id(UUID.randomUUID());
  QuizResult quizResult = quizResultBuilder.name("name").build();

  @Nested
  class POJOTests {
    @Test
    void builderUninitialized_whenInvoked_returnsObjectWithExpectedValues() {
      // Setup
      quizResultBuilder = QuizResult.builder();
      Set<String> fieldNames =
          Arrays.stream(QuizResult.class.getDeclaredFields())
              .map(Field::getName)
              .sorted(String::compareTo)
              .collect(Collectors.toCollection(LinkedHashSet::new));

      // Execution
      quizResult = quizResultBuilder.build();

      // Validation
      assertNotNull(quizResult);
      assertAll(
          () -> assertNullAndLog(quizResult.getCompletedPrompts(), "completedPrompts"),
          () -> assertEqualsAndLog(0, quizResult.getCorrectAnswers(), "correctAnswers"),
          () -> assertNullAndLog(quizResult.getCreatedDateTime(), "createdDateTime"),
          () -> assertNullAndLog(quizResult.getId(), "id"),
          () -> assertNullAndLog(quizResult.getName(), "name"),
          () -> assertEqualsAndLog(0, quizResult.getPercentage(), "percentage"),
          () -> assertEqualsAndLog(0, quizResult.getPromptCount(), "promptCount"),
          () -> assertEqualsAndLog(0, quizResult.getTimeMinutes(), "timeMinutes"),
          () -> assertEqualsAndLog(0, quizResult.getTimeSeconds(), "timeSeconds"),

          // Ensure that we remember to update the UT as we add new fields, logged with the
          // ...AndLog methods above.
          () ->
              assertEquals(
                  testedFields.stream().sorted().toList(), fieldNames.stream().sorted().toList()));
    }

    /** Ensure toString output would be helpful for debugging. */
    @Test
    void toString_whenInvoked_includesAllExpectedFields() {
      // Setup
      quizResult = QuizResult.builder().build();

      // Execute
      String result = quizResult.toString();

      // Validation
      assertEquals(
          "QuizResult(id=null, name=null, createdDateTime=null, promptCount=0, correctAnswers=0, "
              + "percentage=0, timeMinutes=0, timeSeconds=0, completedPrompts=null)",
          result);
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class NameTest {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY name GoEs HeRE", " "})
      void testGetterViaBuilder(String name) {
        // Setup
        quizResult = quizResultBuilder.name(name).build();

        // Execution
        String result = quizResult.getName();

        // Validation
        assertEquals(name, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class PromptCountTest {

      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
      void testGetterViaBuilder(int value) {
        // Setup
        quizResult = quizResultBuilder.promptCount(value).build();

        // Execution
        int result = quizResult.getPromptCount();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class CorrectAnswersTest {

      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
      void testGetterViaBuilder(int value) {
        // Setup
        quizResult = quizResultBuilder.correctAnswers(value).build();

        // Execution
        int result = quizResult.getCorrectAnswers();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class PercentageTest {

      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
      void testGetterViaBuilder(int value) {
        // Setup
        quizResult = quizResultBuilder.percentage(value).build();

        // Execution
        int result = quizResult.getPercentage();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class TimeMinutesTest {

      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
      void testGetterViaBuilder(int value) {
        // Setup
        quizResult = quizResultBuilder.timeMinutes(value).build();

        // Execution
        int result = quizResult.getTimeMinutes();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class TimeSecondsTest {

      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
      void testGetterViaBuilder(int value) {
        // Setup
        quizResult = quizResultBuilder.timeSeconds(value).build();

        // Execution
        int result = quizResult.getTimeSeconds();

        // Validation
        assertEquals(value, result);
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
      final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

      // Validation
      AnnotationValidationUtils.assertEmpty(violations);
    }

    @Nested
    class NameTest {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      void whenNameBlank_thenNotValid(String name) {
        // Setup
        quizResult = quizResultBuilder.name(name).build();

        // Execution
        final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

        // Validation
        AnnotationValidationUtils.assertOneViolation("name must not be blank", violations);
      }
    }

    @Nested
    class CorrectAnswersTest {
      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1})
      void whenNegative_thenNotValid(int value) {
        // Setup
        quizResult = quizResultBuilder.correctAnswers(value).build();

        // Execution
        final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "correctAnswers must be greater than or equal to 0", violations);
      }
    }

    @Nested
    class PromptCountTest {
      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1})
      void whenNegativeAndLessThanCorrectCount_thenNotValidForMultipleReasons(int value) {
        // Setup
        quizResult = quizResultBuilder.promptCount(value).build();
        String[] expectedViolations =
            new String[] {
              " correctAnswers must not be larger than promptCount",
              "promptCount must be greater than or equal to 0",
            };

        // Execution
        final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

        // Validation
        AnnotationValidationUtils.assertEquivalentViolations(
            Arrays.asList(expectedViolations), violations);
      }
    }

    @Nested
    class TimeMinutesTest {
      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1})
      void whenNegative_thenNotValid(int value) {
        // Setup
        quizResult = quizResultBuilder.timeMinutes(value).build();

        // Execution
        final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "timeMinutes must be greater than or equal to 0", violations);
      }
    }

    @Nested
    class TimeSecondsTest {
      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1})
      void whenNegative_thenNotValid(int value) {
        // Setup
        quizResult = quizResultBuilder.timeSeconds(value).build();

        // Execution
        final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "timeSeconds must be greater than or equal to 0", violations);
      }
    }

    @Nested
    class PercentageTest {
      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1})
      void whenNegative_thenNotValid(int value) {
        // Setup
        quizResult = quizResultBuilder.percentage(value).build();

        // Execution
        final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "percentage must be greater than or equal to 0", violations);
      }

      @ParameterizedTest
      @ValueSource(ints = {101, 102, Integer.MAX_VALUE})
      void whenGreaterThanWhole_thenNotValid(int value) {
        // Setup
        quizResult = quizResultBuilder.percentage(value).build();

        // Execution
        final Set<ConstraintViolation<QuizResult>> violations = validator.validate(quizResult);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "percentage must be less than or equal to 100", violations);
      }
    }
  }

  private void assertNullAndLog(Object actualValue, String fieldName) {
    assertNull(actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertEqualsAndLog(int expectedValue, int actualValue, String fieldName) {
    assertEquals(expectedValue, actualValue, fieldName);
    testedFields.add(fieldName);
  }
}
