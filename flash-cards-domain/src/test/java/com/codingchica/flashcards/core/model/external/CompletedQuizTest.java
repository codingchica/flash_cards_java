package com.codingchica.flashcards.core.model.external;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.util.AnnotationValidationUtils;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CompletedQuizTest {

  private List<String> answers = new ArrayList<>();

  /**
   * A collection to allow tests that should include validation every field to enforce that new
   * fields are also added.
   */
  private Set<String> testedFields = new TreeSet<>();

  private CompletedQuiz.Builder completedQuizBuilder = CompletedQuiz.builder();
  CompletedQuiz completedQuiz =
      completedQuizBuilder.name("Name goes HERE").answers(answers).build();

  @BeforeEach
  void setup() {
    answers.add("1");
  }

  @Nested
  class POJOTests {
    @Test
    void builderUninitialized_whenInvoked_returnsObjectWithExpectedValues() {
      // Setup
      completedQuizBuilder = CompletedQuiz.builder();
      Set<String> fieldNames =
          Arrays.stream(CompletedQuiz.class.getDeclaredFields())
              .map(Field::getName)
              .sorted(String::compareTo)
              .collect(Collectors.toCollection(LinkedHashSet::new));

      // Execution
      CompletedQuiz completedQuiz = completedQuizBuilder.build();

      // Validation
      assertNotNull(completedQuiz);
      assertAll(
          () -> assertNullAndLog(completedQuiz.getAnswers(), "answers"),
          () -> assertFalseAndLog(completedQuiz.isInlineGrading(), "inlineGrading"),
          () -> assertNullAndLog(completedQuiz.getName(), "name"),

          // Ensure that we remember to update the UT as we add new fields, logged with the
          // ...AndLog methods above.
          () -> assertEquals(testedFields, fieldNames));
    }

    @Test
    void noArgConstructor_whenInvoked_returnsObjectWithExpectedValues() {
      // Setup
      Set<String> fieldNames =
          Arrays.stream(CompletedQuiz.class.getDeclaredFields())
              .map(Field::getName)
              .sorted(String::compareTo)
              .collect(Collectors.toCollection(LinkedHashSet::new));

      // Execution
      completedQuiz = new CompletedQuiz();

      // Validation
      assertNotNull(completedQuiz);
      assertAll(
          () -> assertNullAndLog(completedQuiz.getAnswers(), "answers"),
          () -> assertFalseAndLog(completedQuiz.isInlineGrading(), "inlineGrading"),
          () -> assertNullAndLog(completedQuiz.getName(), "name"),

          // Ensure that we remember to update the UT as we add new fields, logged with the
          // ...AndLog methods above.
          () -> assertEquals(testedFields, fieldNames));
    }

    /** Ensure toString output would be helpful for debugging. */
    @Test
    void toString_whenInvoked_includesAllExpectedFields() {
      // Setup
      completedQuiz = CompletedQuiz.builder().build();

      // Execute
      String result = completedQuiz.toString();

      // Validation
      assertEquals("CompletedQuiz(name=null, answers=null, inlineGrading=false)", result);
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class NameTest {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY name GoEs HeRE", " "})
      void testGetterViaBuilder(String name) {
        // Setup
        completedQuiz = completedQuizBuilder.name(name).build();

        // Execution
        String result = completedQuiz.getName();

        // Validation
        assertEquals(name, result);
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY name GoEs HeRE", " "})
      void testGetterViaSetter(String name) {
        // Setup
        completedQuiz.setName(name);

        // Execution
        String result = completedQuiz.getName();

        // Validation
        assertEquals(name, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class InlineGradingTest {

      @ParameterizedTest
      @ValueSource(booleans = {true, false})
      void testGetterViaBuilder(boolean value) {
        // Setup
        completedQuiz = completedQuizBuilder.inlineGrading(value).build();

        // Execution
        boolean result = completedQuiz.isInlineGrading();

        // Validation
        assertEquals(value, result);
      }

      @ParameterizedTest
      @ValueSource(booleans = {true, false})
      void testGetterViaSetter(boolean value) {
        // Setup
        completedQuiz.setInlineGrading(value);

        // Execution
        boolean result = completedQuiz.isInlineGrading();

        // Validation
        assertEquals(value, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class AnswersTest {

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY value GoEs HeRE", " "})
      void testGetterViaBuilder(String value) {
        // Setup
        List<String> answers = new ArrayList<>();
        answers.add(value);
        completedQuiz = completedQuizBuilder.answers(answers).build();

        // Execution
        List<String> result = completedQuiz.getAnswers();

        // Validation
        assertSame(answers, result, value);
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"MY value GoEs HeRE", " "})
      void testGetterViaSetter(String value) {
        // Setup
        List<String> answers = new ArrayList<>();
        answers.add(value);
        completedQuiz.setAnswers(answers);

        // Execution
        List<String> result = completedQuiz.getAnswers();

        // Validation
        assertSame(answers, result, value);
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
      final Set<ConstraintViolation<CompletedQuiz>> violations = validator.validate(completedQuiz);

      // Validation
      AnnotationValidationUtils.assertEmpty(violations);
    }

    @Nested
    class NameTest {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(
          strings = {
            " ",
          })
      void whenBlank_thenNotValid(String value) {
        // Setup
        completedQuiz = completedQuizBuilder.name(value).build();

        // Execution
        final Set<ConstraintViolation<CompletedQuiz>> violations =
            validator.validate(completedQuiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation("name must not be blank", violations);
      }
    }

    @Nested
    class AnswersTest {
      @ParameterizedTest
      @NullAndEmptySource
      void whenListEmpty_thenNotValid(List<String> value) {
        // Setup
        completedQuiz = completedQuizBuilder.answers(value).build();

        // Execution
        final Set<ConstraintViolation<CompletedQuiz>> violations =
            validator.validate(completedQuiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation("answers must not be empty", violations);
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      void whenValueEmpty_thenNotValid(String value) {
        // Setup
        answers.add(value);

        // Execution
        final Set<ConstraintViolation<CompletedQuiz>> violations =
            validator.validate(completedQuiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "answers[1].<list element> must not be blank", violations);
      }
    }
  }

  private void assertFalseAndLog(boolean actualValue, String fieldName) {
    assertFalse(actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertNullAndLog(Object actualValue, String fieldName) {
    assertNull(actualValue, fieldName);
    testedFields.add(fieldName);
  }
}
