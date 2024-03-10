package com.codingchica.flashcards.core.config;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.util.AnnotationValidationUtils;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for the POJO methods within the FlashCardsConfiguration class. */
class FlashCardsConfigurationTest {
  private FlashCardsConfiguration.Builder flashCardsConfigurationBuilder =
      ConfigFactory.flashCardsConfigurationBuilder();
  private FlashCardsConfiguration flashCardsConfiguration = ConfigFactory.flashCardsConfiguration();
  private String flashCardGroupMapKey = "some key";
  private Map<String, String> prompts = new HashMap<>();
  private FlashCardGroup.Builder flashCardGroupBuilder = ConfigFactory.flashCardGroupBuilder();
  private FlashCardGroup flashCardGroup = ConfigFactory.flashCardGroup();

  @BeforeEach
  void setup() {
    prompts.put("A", "B");
  }

  @Nested
  class POJOTests {
    @Test
    void builderUninitialized_whenInvoked_returnsObject() {
      // Setup
      flashCardsConfigurationBuilder = FlashCardsConfiguration.builder();

      // Execution
      FlashCardsConfiguration flashCardsConfiguration = flashCardsConfigurationBuilder.build();

      // Validation
      assertNotNull(flashCardsConfiguration);
      assertAll(
          () -> assertNull(flashCardsConfiguration.getFlashCardGroupMap(), "flashCardGroupMap"));
    }

    /** Ensure toString output would be helpful for debugging. */
    @Test
    void toString_whenInvoked_includesAllExpectedFields() {
      // Setup
      FlashCardsConfiguration flashCardsConfiguration = new FlashCardsConfiguration();

      // Execute
      String result = flashCardsConfiguration.toString();

      // Validation
      assertEquals("FlashCardsConfiguration(flashCardGroupMap=null)", result);
    }

    /** Ensure that Lombok annotations are set up as expected. */
    @Nested
    class FlashCardGroupMapTest {

      @Test
      void testGetterViaBuilder() {
        // Setup
        Map<String, FlashCardGroup> flashCardGroupMap = new TreeMap<>();
        FlashCardGroup flashCardGroup = ConfigFactory.flashCardGroup();
        flashCardGroupMap.put("Test1", flashCardGroup);
        flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap);
        flashCardsConfiguration = flashCardsConfigurationBuilder.build();

        // Execution
        Map<String, FlashCardGroup> result = flashCardsConfiguration.getFlashCardGroupMap();

        // Validation
        assertSame(flashCardGroupMap, result);
      }

      @Test
      void testGetterViaSetter() {
        // Setup
        Map<String, FlashCardGroup> flashCardGroupMap = new TreeMap<>();
        FlashCardGroup flashCardGroup = ConfigFactory.flashCardGroup();
        flashCardGroupMap.put("Test1", flashCardGroup);
        flashCardsConfiguration.setFlashCardGroupMap(flashCardGroupMap);

        // Execution
        Map<String, FlashCardGroup> result = flashCardsConfiguration.getFlashCardGroupMap();

        // Validation
        assertSame(flashCardGroupMap, result);
      }
    }

    @Nested
    class BuilderTest {

      /** Ensure toString output would be helpful for debugging. */
      @Test
      void toString_whenInvoked_includesAllExpectedFields() {
        // Setup
        flashCardsConfigurationBuilder = FlashCardsConfiguration.builder();

        // Execute
        String result = flashCardsConfigurationBuilder.toString();

        // Validation
        assertEquals("FlashCardsConfiguration.Builder(flashCardGroupMap=null)", result);
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
      final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
          validator.validate(flashCardsConfiguration);

      // Validation
      AnnotationValidationUtils.assertEmpty(violations);
    }

    @Nested
    class FlashCardGroupMapTest {
      @ParameterizedTest
      @NullAndEmptySource
      void whenFlashCardGroupMapNull_thenNotValid(Map<String, FlashCardGroup> flashCardGroupMap) {
        // Setup
        flashCardsConfiguration =
            flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap).build();

        // Execution
        final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
            validator.validate(flashCardsConfiguration);

        // Validation
        AnnotationValidationUtils.assertOneViolation(
            "flashCardGroupMap must not be empty", violations);
      }

      @Nested
      class KeyTest {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void whenFlashCardGroupMapStringBlank_thenNotValid(String key) {
          // Setup
          String sanitizedKey = (key == null) ? "" : key;
          Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
          flashCardGroupMap.put(key, flashCardGroup);
          flashCardsConfiguration =
              flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap).build();

          // Execution
          final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
              validator.validate(flashCardsConfiguration);

          // Validation
          AnnotationValidationUtils.assertOneViolation(
              String.format("flashCardGroupMap<K>[%s].<map key> must not be blank", sanitizedKey),
              violations);
        }

        @ParameterizedTest
        @ValueSource(strings = {"a\tb", "cd\r", "\fef", "A-b", "c;D"})
        void whenFlashCardGroupMapStringInvalidChars_thenNotValid(String key) {
          // Setup
          String sanitizedKey = (key == null) ? "" : key;
          Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
          flashCardGroupMap.put(key, flashCardGroup);
          flashCardsConfiguration =
              flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap).build();

          // Execution
          final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
              validator.validate(flashCardsConfiguration);

          // Validation
          AnnotationValidationUtils.assertOneViolation(
              String.format(
                  "flashCardGroupMap<K>[%s].<map key> must contain only alpha-numeric characters"
                      + " and spaces",
                  sanitizedKey),
              violations);
        }

        @ParameterizedTest
        @ValueSource(ints = {31, 32, 33})
        void whenFlashCardGroupMapStringTooLong_thenNotValid(int keyLength) {
          // Setup
          String key = StringUtils.leftPad("", keyLength, "A");
          System.out.println("key=" + key);
          Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
          flashCardGroupMap.put(key, flashCardGroup);
          flashCardsConfiguration =
              flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap).build();

          // Execution
          final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
              validator.validate(flashCardsConfiguration);

          // Validation
          AnnotationValidationUtils.assertOneViolation(
              String.format(
                  "flashCardGroupMap<K>[%s].<map key> must be 30 characters or less", key),
              violations);
        }
      }

      @Nested
      class ValueTest {

        @ParameterizedTest
        @NullSource
        void whenFlashCardGroupMapValueNull_thenNotValid(FlashCardGroup flashCardGroupValue) {
          // Setup
          Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
          flashCardGroupMap.put(flashCardGroupMapKey, flashCardGroupValue);
          flashCardsConfiguration =
              flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap).build();

          // Execution
          final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
              validator.validate(flashCardsConfiguration);

          // Validation
          AnnotationValidationUtils.assertOneViolation(
              String.format(
                  "flashCardGroupMap[%s].<map value> must not be null", flashCardGroupMapKey),
              violations);
        }

        @Test
        void whenFlashCardGroupMapValueMinMaxReversed_thenNotValid() {
          // Setup
          Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
          flashCardGroup = flashCardGroupBuilder.maximumPrompts(2).minimumPrompts(3).build();
          flashCardGroupMap.put(flashCardGroupMapKey, flashCardGroup);
          flashCardsConfiguration =
              flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap).build();

          // Execution
          final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
              validator.validate(flashCardsConfiguration);

          // Validation
          AnnotationValidationUtils.assertOneViolation(
              String.format(
                  "flashCardGroupMap[%s] minimumPrompts must not be larger than maximumPrompts",
                  flashCardGroupMapKey),
              violations);
        }

        @Test
        void whenFlashCardGroupMapValueNotValid_thenNotValid() {
          // Setup
          Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
          flashCardGroup = flashCardGroupBuilder.prompts(null).build();
          flashCardGroupMap.put(flashCardGroupMapKey, flashCardGroup);
          flashCardsConfiguration =
              flashCardsConfigurationBuilder.flashCardGroupMap(flashCardGroupMap).build();

          // Execution
          final Set<ConstraintViolation<FlashCardsConfiguration>> violations =
              validator.validate(flashCardsConfiguration);

          // Validation
          AnnotationValidationUtils.assertOneViolation(
              String.format(
                  "flashCardGroupMap[%s].prompts must not be empty", flashCardGroupMapKey),
              violations);
        }
      }
    }
  }
}
