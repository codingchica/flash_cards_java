package com.codingchica.flashcards.core.config;

import static com.codingchica.flashcards.util.AnnotationValidationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.util.AnnotationValidationUtils;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

/** Unit tests for the FlashCardGroup class. */
class FlashCardGroupTest {
  /** An instance of the object under test. */
  private FlashCardGroup flashCardGroup = ConfigFactory.flashCardGroup();

  private Map<String, String> prompts = new TreeMap<>();

  /** A builder to easily create new instances of the class under test. */
  private FlashCardGroup.Builder flashCardGroupBuilder =
      FlashCardGroup.builder().prompts(prompts).maxDuration(Duration.ofSeconds(30));

  /**
   * A collection to allow tests that should include validation every field to enforce that new
   * fields are also added.
   */
  private Set<String> testedFields = new TreeSet<>();

  @BeforeEach
  void setup() {
    prompts.put("key", "value");
  }

  @Nested
  class POJOTests {
    @Test
    void builderUninitialized_whenInvoked_returnsObjectWithExpectedValues() {
      // Setup
      flashCardGroupBuilder = FlashCardGroup.builder();
      Set<String> fieldNames =
          Arrays.stream(FlashCardGroup.class.getDeclaredFields())
              .map(Field::getName)
              .sorted(String::compareTo)
              .collect(Collectors.toCollection(LinkedHashSet::new));
      testedFields.add("maximumPrompts");
      testedFields.add("prompts");

      // Execution
      FlashCardGroup flashCardGroup = flashCardGroupBuilder.build();

      // Validation
      assertNotNull(flashCardGroup);
      assertAll(
          () -> assertEqualsAndLog(0, flashCardGroup.getMaximumPrompts(), "maximumPrompts"),
          () -> assertEqualsAndLog(0, flashCardGroup.getMaximumPrompts(), "minimumPrompts"),
          () -> assertNullAndLog(flashCardGroup.getPrompts(), "prompts"),
          () -> assertNullAndLog(flashCardGroup.getMaxDuration(), "maxDuration"),

          // Ensure that we remember to update the UT as we add new fields, logged with the
          // ...AndLog methods above.
          () -> assertEquals(testedFields, fieldNames));
    }

    /** Ensure toString output would be helpful for debugging. */
    @Test
    void toString_whenInvoked_includesAllExpectedFields() {
      // Setup
      flashCardGroup = new FlashCardGroup();

      // Execute
      String result = flashCardGroup.toString();

      // Validation
      assertEquals(
          "FlashCardGroup(maximumPrompts=0, minimumPrompts=0, maxDuration=null, prompts=null)",
          result);
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class MaximumPromptsTest {

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            -1,
            0,
            1,
            Integer.MAX_VALUE,
          })
      void testGetter(int value) {
        // Setup
        flashCardGroup = flashCardGroupBuilder.maximumPrompts(value).build();

        // Execution
        int result = flashCardGroup.getMaximumPrompts();

        // Validation
        assertEquals(value, result);
      }

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            0,
            -1,
            1,
          })
      void testGetterViaSetter(int maxPrompts) {
        // Setup
        flashCardGroup.setMaximumPrompts(maxPrompts);

        // Execution
        int result = flashCardGroup.getMaximumPrompts();

        // Validation
        assertEquals(maxPrompts, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class MinimumPromptsTest {

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            -1,
            0,
            1,
            Integer.MAX_VALUE,
          })
      void testGetter(int value) {
        // Setup
        flashCardGroup = flashCardGroupBuilder.minimumPrompts(value).build();

        // Execution
        int result = flashCardGroup.getMinimumPrompts();

        // Validation
        assertEquals(value, result);
      }

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            0,
            -1,
            1,
          })
      void testGetterViaSetter(int minPrompts) {
        // Setup
        flashCardGroup.setMinimumPrompts(minPrompts);

        // Execution
        int result = flashCardGroup.getMinimumPrompts();

        // Validation
        assertEquals(minPrompts, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class MaxDurationTest {

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            0,
            -1,
            1,
          })
      void testGetterViaBuilder(int seconds) {
        // Setup
        Duration duration = Duration.ofSeconds(seconds);
        flashCardGroup = flashCardGroupBuilder.maxDuration(duration).build();

        // Execution
        Duration result = flashCardGroup.getMaxDuration();

        // Validation
        assertEquals(duration, result);
      }

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            0,
            -1,
            1,
          })
      void testGetterViaSetter(int seconds) {
        // Setup
        Duration duration = Duration.ofSeconds(seconds);
        flashCardGroup.setMaxDuration(duration);

        // Execution
        Duration result = flashCardGroup.getMaxDuration();

        // Validation
        assertEquals(duration, result);
      }
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class PromptsTest {

      @ParameterizedTest
      @CsvSource(
          value = {"1+1:2+2,2:4"},
          delimiter = ',')
      void testGetterViaBuilder(String keys, String values) {
        // Setup
        Map<String, String> prompts = new TreeMap<>();
        String[] keyElements = StringUtils.split(keys, ":");
        String[] valueElements = StringUtils.split(values, ":");
        assertEquals(keyElements.length, valueElements.length, "size");
        for (int i = 0; i < keyElements.length && i < valueElements.length; i++) {
          prompts.put(keyElements[i], valueElements[i]);
        }
        flashCardGroup = flashCardGroupBuilder.prompts(prompts).build();

        // Execution
        Map<String, String> result = flashCardGroup.getPrompts();

        // Validation
        assertEquals(prompts, result);
      }

      @ParameterizedTest
      @CsvSource(
          value = {"1+1:2+2,2:4"},
          delimiter = ',')
      void testGetterViaSetter(String keys, String values) {
        // Setup
        Map<String, String> prompts = new TreeMap<>();
        String[] keyElements = StringUtils.split(keys, ":");
        String[] valueElements = StringUtils.split(values, ":");
        assertEquals(keyElements.length, valueElements.length, "size");
        for (int i = 0; i < keyElements.length && i < valueElements.length; i++) {
          prompts.put(keyElements[i], valueElements[i]);
        }
        flashCardGroup.setPrompts(prompts);

        // Execution
        Map<String, String> result = flashCardGroup.getPrompts();

        // Validation
        assertEquals(prompts, result);
      }
    }

    @Nested
    class BuilderTest {

      /** Ensure toString output would be helpful for debugging. */
      @Test
      void toString_whenInvoked_includesAllExpectedFields() {
        // Setup
        flashCardGroupBuilder = FlashCardGroup.builder();

        // Execute
        String result = flashCardGroupBuilder.toString();

        // Validation
        assertEquals(
            "FlashCardGroup.Builder(maximumPrompts=0, minimumPrompts=0, maxDuration=null,"
                + " prompts=null)",
            result);
      }
    }
  }

  @Nested
  class ValidationTest {
    private final Validator validator = BaseValidator.newValidator();

    @Nested
    class MaximumPromptsValidationTest {

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            -5,
            -1,
          })
      void whenNegative_thenNotValid(int value) {
        // Setup
        String[] expectedViolations = {
          "minimumPrompts must be greater than or equal to 0",
          "maximumPrompts must be greater than or equal to 0"
        };
        flashCardGroup =
            flashCardGroupBuilder.maximumPrompts(value).minimumPrompts(Integer.MIN_VALUE).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertEquivalentViolations(Arrays.asList(expectedViolations), violations);
      }

      @ParameterizedTest
      @ValueSource(ints = {0, 1, 10, Integer.MAX_VALUE})
      void whenValidValue_thenNoValidationError(int value) {
        // Setup
        flashCardGroup = flashCardGroupBuilder.maximumPrompts(value).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertEmpty(violations);
      }
    }

    @Nested
    class MinimumPromptsValidationTest {

      @ParameterizedTest
      @ValueSource(
          ints = {
            Integer.MIN_VALUE,
            -5,
            -1,
          })
      void whenNegative_thenNotValid(int value) {
        // Setup
        flashCardGroup =
            flashCardGroupBuilder.maximumPrompts(Integer.MAX_VALUE).minimumPrompts(value).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("minimumPrompts must be greater than or equal to 0", violations);
      }

      @ParameterizedTest
      @ValueSource(ints = {0, 1, 10, Integer.MAX_VALUE})
      void whenValidValue_thenValid(int value) {
        // Setup
        flashCardGroup = flashCardGroupBuilder.maximumPrompts(value).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertEmpty(violations);
      }
    }

    @Nested
    class PromptsValidationTest {

      @ParameterizedTest
      @NullAndEmptySource
      void whenNullOrEmpty_thenValidationError(Map<String, String> value) {
        // Setup
        flashCardGroup = flashCardGroupBuilder.prompts(value).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("prompts must not be empty", violations);
      }

      @ParameterizedTest
      @NullSource
      void whenKeyNull_thenValidationError(String key) {
        // Setup
        prompts = new HashMap<>();
        prompts.put(key, "valueGoesHere");
        flashCardGroup = flashCardGroupBuilder.prompts(prompts).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("prompts<K>[].<map key> must not be blank", violations);
      }

      @ParameterizedTest
      @EmptySource
      @ValueSource(strings = {" "})
      void whenKeyBlank_thenValidationError(String key) {
        // Setup
        prompts.put(key, "valueGoesHere");
        flashCardGroup = flashCardGroupBuilder.prompts(prompts).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation(
            String.format("prompts<K>[%s].<map key> must not be blank", key), violations);
      }

      @ParameterizedTest
      @ValueSource(
          strings = {
            "111111111122222222223333333333444444444455555555556",
            "1111111111222222222233333333334444444444555555555566",
          })
      void whenKeyTooLong_thenValidationError(String key) {
        // Setup
        prompts.put(key, "valueGoesHere");
        flashCardGroup = flashCardGroupBuilder.prompts(prompts).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation(
            String.format("prompts<K>[%s].<map key> must be 50 characters or less", key),
            violations);
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" "})
      void whenValueBlank_thenValidationError(String value) {
        // Setup
        prompts.put("key", value);
        flashCardGroup = flashCardGroupBuilder.prompts(prompts).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("prompts[key].<map value> must not be blank", violations);
      }

      @ParameterizedTest
      @ValueSource(
          strings = {
            "111111111122222222223333333333444444444455555555556",
            "1111111111222222222233333333334444444444555555555566",
          })
      void whenValueTooLong_thenValidationError(String value) {
        // Setup
        prompts.put("key", value);
        flashCardGroup = flashCardGroupBuilder.prompts(prompts).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("prompts[key].<map value> must be 50 characters or less", violations);
      }

      @Test
      void whenValidValue_thenNoValidationError() {
        // Setup
        flashCardGroup = flashCardGroupBuilder.prompts(prompts).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertEmpty(violations);
      }

      @Test
      void whenFlashCardGroupMinMaxReversed_thenNotValid() {
        // Setup
        Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
        flashCardGroup = flashCardGroupBuilder.maximumPrompts(2).minimumPrompts(3).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            " minimumPrompts must not be larger than maximumPrompts", violations);
      }
    }

    @Nested
    class MaxDurationValidationTest {

      @Test
      void whenNull_thenValidationError() {
        // Setup
        flashCardGroup = flashCardGroupBuilder.maxDuration(null).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("maxDuration must not be null", violations);
      }

      @Test
      void whenTooLarge_thenValidationError() {
        // Setup
        Duration duration = Duration.ofHours(1).plus(Duration.ofSeconds(1));
        flashCardGroup = flashCardGroupBuilder.maxDuration(duration).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("maxDuration must be shorter than or equal to 1 hour", violations);
      }

      @Test
      void whenTooSmall_thenValidationError() {
        // Setup
        Duration duration = Duration.ofSeconds(9);
        flashCardGroup = flashCardGroupBuilder.maxDuration(duration).build();

        // Execution
        final Set<ConstraintViolation<FlashCardGroup>> violations =
            validator.validate(flashCardGroup);

        // Validation
        assertOneViolation("maxDuration must be longer than or equal to 10 seconds", violations);
      }
    }

    /** Generic happy-path scenario. */
    @Test
    void happyPath() {
      // Setup

      // Execution
      final Set<ConstraintViolation<FlashCardGroup>> violations =
          validator.validate(flashCardGroup);

      // Validation
      assertEmpty(violations);
    }
  }

  private void assertEqualsAndLog(int expectedValue, int actualValue, String fieldName) {
    assertEquals(expectedValue, actualValue, fieldName);
    testedFields.add(fieldName);
  }

  private void assertNullAndLog(Object actualValue, String fieldName) {
    assertNull(actualValue, fieldName);
    testedFields.add(fieldName);
  }
}
