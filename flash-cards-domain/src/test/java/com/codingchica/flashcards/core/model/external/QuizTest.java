package com.codingchica.flashcards.core.model.external;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.util.AnnotationValidationUtils;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class QuizTest {
  private List<Map.Entry<String, String>> prompts = new ArrayList<>();
  private Quiz.Builder quizBuilder = Quiz.builder();
  private Quiz quiz =
      quizBuilder
          .name("Name Goes Here")
          .id(UUID.randomUUID())
          .createdDateTime(Instant.now())
          .prompts(prompts)
          .build();

  @BeforeEach
  void setup() {
    Map<String, String> promptsMap = new HashMap<>();
    promptsMap.put("Key", "Value");

    prompts.addAll(promptsMap.entrySet());
  }

  @Nested
  class IdTest {

    @ParameterizedTest
    @ValueSource(
        strings = {"e5d9fe4a-721c-40e3-925a-29ff4b348375", "df5012e8-58c9-43c2-909c-9814ff411c41"})
    void whenSetInBuilder_thenSameReturnedInGetter(String id) {
      // Setup
      UUID uuid = UUID.fromString(id);
      quizBuilder.id(uuid);

      // Execution
      Quiz quiz = quizBuilder.build();

      // Validation
      assertNotNull(quiz);
      assertEquals(uuid, quiz.getId());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"e5d9fe4a-721c-40e3-925a-29ff4b348375", "df5012e8-58c9-43c2-909c-9814ff411c41"})
    void whenSetInSetter_thenSameReturnedInGetter(String id) {
      // Setup
      UUID uuid = UUID.fromString(id);
      quiz.setId(uuid);

      // Execution
      UUID result = quiz.getId();

      // Validation
      assertEquals(uuid, result);
    }
  }

  @Nested
  class NameTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "some value goes here"})
    void whenSetInBuilder_thenSameReturnedInGetter(String name) {
      // Setup
      quizBuilder.name(name);

      // Execution
      Quiz quiz = quizBuilder.build();

      // Validation
      assertNotNull(quiz);
      assertEquals(name, quiz.getName());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "some value goes here"})
    void whenSetInSetter_thenSameReturnedInGetter(String name) {
      // Setup
      quiz.setName(name);

      // Execution
      String result = quiz.getName();

      // Validation
      assertEquals(name, result);
    }
  }

  @Nested
  class CreatedDateTimeTest {
    @Test
    void whenSetInBuilder_thenSameReturnedInGetter() {
      // Setup
      Instant zonedDateTime = Instant.now();
      quizBuilder.createdDateTime(zonedDateTime);

      // Execution
      Quiz quiz = quizBuilder.build();

      // Validation
      assertNotNull(quiz);
      assertSame(zonedDateTime, quiz.getCreatedDateTime());
    }

    @Test
    void whenSetInSetter_thenSameReturnedInGetter() {
      // Setup
      Instant zonedDateTime = Instant.now();
      quiz.setCreatedDateTime(zonedDateTime);

      // Execution
      Instant result = quiz.getCreatedDateTime();

      // Validation
      assertSame(zonedDateTime, result);
    }
  }

  @Nested
  class PromptsTest {
    @Test
    void whenSetInBuilder_thenSameReturnedInGetter() {
      // Setup
      List<Map.Entry<String, String>> prompts = new ArrayList<>();
      quizBuilder.prompts(prompts);

      // Execution
      Quiz quiz = quizBuilder.build();

      // Validation
      assertNotNull(quiz);
      assertSame(prompts, quiz.getPrompts());
    }

    @Test
    void whenSetInSetter_thenSameReturnedInGetter() {
      // Setup
      List<Map.Entry<String, String>> prompts = new ArrayList<>();
      quiz.setPrompts(prompts);

      // Execution
      List<Map.Entry<String, String>> result = quiz.getPrompts();

      // Validation
      assertSame(prompts, result);
    }
  }

  @Nested
  class BuilderTest {
    @Test
    void toString_whenInvoked_thenReturnsExpectedValue() {
      // Setup
      quizBuilder = Quiz.builder();

      // Execution
      String result = quizBuilder.toString();

      // Validation
      assertEquals("Quiz.Builder(id=null, name=null, createdDateTime=null, prompts=null)", result);
    }
  }

  @Nested
  class NoArgConstructorTest {
    @Test
    void whenInvoked_thenReturnsExpectedValues() {
      // Execution
      Quiz result = new Quiz();

      // Validation
      assertAll(
          () -> assertNull(result.getId(), "id"),
          () -> assertNull(result.getName(), "name"),
          () -> assertNull(result.getPrompts(), "prompts"));
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
      final Set<ConstraintViolation<Quiz>> violations = validator.validate(quiz);

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
        quiz = quizBuilder.name(value).build();

        // Execution
        final Set<ConstraintViolation<Quiz>> violations = validator.validate(quiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation("name must not be blank", violations);
      }
    }

    @Nested
    class UUIDTest {
      @ParameterizedTest
      @NullSource
      void whenNull_thenNotValid(UUID value) {
        // Setup
        quiz = quizBuilder.id(value).build();

        // Execution
        final Set<ConstraintViolation<Quiz>> violations = validator.validate(quiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation("id must not be null", violations);
      }
    }

    @Nested
    class PromptsTest {
      @ParameterizedTest
      @NullAndEmptySource
      void whenEmpty_thenNotValid(List<Map.Entry<String, String>> value) {
        // Setup
        quiz = quizBuilder.prompts(value).build();

        // Execution
        final Set<ConstraintViolation<Quiz>> violations = validator.validate(quiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation("prompts must not be empty", violations);
      }
    }

    @Nested
    class CreatedDateTimeTest {
      @ParameterizedTest
      @NullSource
      void whenNull_thenNotValid(Instant value) {
        // Setup
        quiz = quizBuilder.createdDateTime(value).build();

        // Execution
        final Set<ConstraintViolation<Quiz>> violations = validator.validate(quiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "createdDateTime must not be null", violations);
      }

      @Test
      void whenFuture_thenNotValid() {
        // Setup
        Instant now = Instant.now();
        Instant futureDateTime = now.plus(Duration.ofMinutes(1));
        quiz = quizBuilder.createdDateTime(futureDateTime).build();

        // Execution
        final Set<ConstraintViolation<Quiz>> violations = validator.validate(quiz);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "createdDateTime must be a date in the past or in the present", violations);
      }
    }
  }
}
