package com.codingchica.flashcards.core.mappers.external;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.core.config.ConfigFactory;
import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class QuizMapperImplTest {
  private Random random = new Random(123l);

  private ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();
  private QuizMapperImpl.Builder quizMapperBuilder = QuizMapperImpl.builder();
  private QuizMapperImpl quizMapper =
      quizMapperBuilder.random(random).uuidGenerator(uuidGenerator).build();
  private FlashCardGroup.Builder flashCardGroupBuilder = ConfigFactory.flashCardGroupBuilder();
  private Map<String, String> originalPrompts = new TreeMap<>();
  private FlashCardGroup flashCardGroup = flashCardGroupBuilder.prompts(originalPrompts).build();
  private String name = "some name goes here";

  @BeforeEach
  void setup() {
    for (int i = 0; i < 3; i++) {
      originalPrompts.put("My Key " + i, "My Value " + 1);
    }
  }

  @Nested
  class GetCopiesCountTest {
    @ParameterizedTest
    @CsvSource(
        value = {
          // No Duplication
          "0,1,1",
          "0,25,1",
          "0,20,1",
          "1,1,1",

          // Duplication to satisfy min
          "2,1,2",
          "5,2,3",
          "20,5,4",
          "20,3,7",
        })
    void whenInvokedWithVariedValues_thenReturnsExpectedOutput(
        int minPrompts, int promptsMapSize, int expectedOutput) {
      // Execution
      int result = QuizMapperImpl.getCopiesCount(minPrompts, promptsMapSize);

      // Validation
      assertEquals(expectedOutput, result);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0})
    void whenPromptsMapSizeToSmall_thenExceptionThrown(int promptMapSize) {
      // Execution
      Executable executable = () -> QuizMapperImpl.getCopiesCount(0, promptMapSize);

      // Validation
      Exception exception = assertThrows(IllegalArgumentException.class, executable);
      assertEquals("promptsMapSize must be greater than 0", exception.getMessage());
    }
  }

  @Nested
  class InternalToExternalQuizMappingTest {

    @Test
    void whenFlashCardGroupNull_thenExceptionReturned() {
      // Setup

      // Execution
      Executable executable = () -> quizMapper.internalToExternalQuizMapping(null);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("flashCardGroup is marked non-null but is null", exception.getMessage());
    }

    @Nested
    class IdTest {

      @Test
      void whenInvoked_thenIdPopulated() {
        // Setup

        // Execution
        Quiz quiz = quizMapper.internalToExternalQuizMapping(flashCardGroup);

        // Validation
        assertNotNull(quiz);
        assertNotNull(quiz.getId());
      }
    }

    @Nested
    class PromptsTest {
      @Test
      void whenPromptsNull_thenTODO() {
        // Setup
        flashCardGroup = flashCardGroupBuilder.prompts(null).build();
        assertNull(flashCardGroup.getPrompts());

        // Execution
        Executable executable = () -> quizMapper.internalToExternalQuizMapping(flashCardGroup);

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("promptsMap is marked non-null but is null", exception.getMessage());
      }

      @ParameterizedTest
      @CsvSource(
          value = {
            // No Change for min
            "0,0,1,1",
            "0,0,3,3",

            // No Duplication
            "0,0,1,1",
            "0,0,25,25",
            "0,0,20,20",
            "0,1,1,1",

            // Duplication to satisfy min
            "2,0,1,2",
            "5,0,2,5",
            "20,0,5,20",
            "20,0,3,20",

            // Limited by max
            "0,3,5,3",
            "3,3,5,3",
            "1,25,30,25"
          })
      void whenMinAndMaxNotPopulated_thenOriginalPromptsReturned(
          int minPrompts, int maxPrompts, int promptsCount, int expectedCount) {
        // Setup
        flashCardGroup =
            flashCardGroupBuilder.minimumPrompts(minPrompts).maximumPrompts(maxPrompts).build();
        originalPrompts.clear();
        for (int i = 0; i < promptsCount; i++) {
          originalPrompts.put("Key" + i, "Value" + i);
        }
        assertEquals(promptsCount, flashCardGroup.getPrompts().size());

        // Execution
        Quiz quiz = quizMapper.internalToExternalQuizMapping(flashCardGroup);

        // Validation
        assertNotNull(quiz);
        System.out.println(quiz.getPrompts());
        assertEquals(expectedCount, quiz.getPrompts().size());
        for (Map.Entry<String, String> promptEntry : quiz.getPrompts()) {
          assertNotNull(promptEntry, "promptEntry");
          assertTrue(
              StringUtils.startsWith(promptEntry.getKey(), "Key"),
              String.format("Key[%s]", promptEntry.getKey()));
          assertTrue(
              StringUtils.startsWith(promptEntry.getValue(), "Value"),
              String.format("Value[%s]", promptEntry.getKey()));

          // Key/Value assignment remains consistent
          String keySuffix = StringUtils.removeStart(promptEntry.getKey(), "Key");
          String valueSuffix = StringUtils.removeStart(promptEntry.getValue(), "Value");
          assertEquals(
              keySuffix, valueSuffix, String.format("Key-Value[%s]", promptEntry.getKey()));
        }
      }
    }
  }

  @Nested
  class BuilderTest {

    @Nested
    class UuidGeneratorTest {

      @Test
      void whenSetInBuilder_thenReturnedInGetter() {
        // Setup
        quizMapperBuilder = quizMapperBuilder.uuidGenerator(uuidGenerator);

        // Execution
        quizMapper = quizMapperBuilder.build();

        // Validation
        assertNotNull(quizMapper);
        assertSame(uuidGenerator, quizMapper.getUuidGenerator());
      }

      @Test
      void whenSetNullInBuilder_thenExceptionThrown() {
        // Setup

        // Execution
        QuizMapperImpl.Builder result = quizMapperBuilder.uuidGenerator(null);

        // Validation
        assertNotNull(result);
      }

      @Test
      void whenNotSetNullInBuilder_thenDefaultReturnedInGetter() {
        // Setup
        quizMapperBuilder = QuizMapperImpl.builder();

        // Execution
        Executable executable = () -> quizMapperBuilder.build();

        // Validation
        assertNotNull(quizMapper);
        assertNotNull(quizMapper.getUuidGenerator());
      }
    }

    @Nested
    class RandomTest {

      @Test
      void whenSetInBuilder_thenReturnedInGetter() {
        // Setup
        quizMapperBuilder = quizMapperBuilder.random(random);

        // Execution
        quizMapper = quizMapperBuilder.build();

        // Validation
        assertNotNull(quizMapper);
        assertSame(random, quizMapper.getRandom());
      }

      @Test
      void whenSetNullInBuilder_thenExceptionThrown() {
        // Setup

        // Execution
        QuizMapperImpl.Builder result = quizMapperBuilder.random(null);

        // Validation
        assertNotNull(result);
      }

      @Test
      void whenNotSetNullInBuilder_thenDefaultReturnedInGetter() {
        // Setup
        quizMapperBuilder = QuizMapperImpl.builder();

        // Execution
        quizMapper = quizMapperBuilder.build();

        // Validation
        assertNotNull(quizMapper);
        assertNotNull(quizMapper.getRandom());
      }
    }

    @Test
    void toString_whenInvoked_returnsExpectedFormat() {
      // Execution
      String result = QuizMapperImpl.builder().toString();

      // Validation
      assertEquals("QuizMapperImpl.Builder(uuidGenerator$value=null, random$value=null)", result);
    }
  }
}
