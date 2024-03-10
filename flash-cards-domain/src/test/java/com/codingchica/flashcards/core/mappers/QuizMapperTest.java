package com.codingchica.flashcards.core.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.core.config.ConfigFactory;
import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.model.external.Quiz;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class QuizMapperTest {
  private String name = "Name goes here";
  private FlashCardGroup flashCardGroup = ConfigFactory.flashCardGroup();
  private final QuizMapper quizMapperImpl = new QuizMapperImpl();
  private UUID uuid = UUID.randomUUID();
  private ZonedDateTime dueDateTime = ZonedDateTime.now().plus(Duration.ofSeconds(60));

  @Nested
  class InternalToExternalQuizMappingTest {
    @Test
    void whenNullInputs_thenNullReturned() {
      // Execution
      Quiz result = quizMapperImpl.internalToExternalQuizMapping(null, null, null, null);

      // Validation
      assertNull(result);
    }

    @Nested
    class NameTest {

      @Test
      void whenNameNotNull_thenSameReturned() {
        // Execution
        Quiz result = quizMapperImpl.internalToExternalQuizMapping(name, null, null, null);

        // Validation
        assertNotNull(result);
        assertEquals(name, result.getName());
      }

      @Test
      void whenNullName_thenNullMappedInResult() {
        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(null, flashCardGroup, uuid, dueDateTime);

        // Validation
        assertNotNull(result);
        assertNull(result.getName());
      }

      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(
          strings = {
            "name goes here",
            "this is my Name",
          })
      void whenNameVaried_thenSameUsed(String name) {
        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(name, flashCardGroup, uuid, dueDateTime);

        // Validation
        assertNotNull(result);
        assertEquals(name, result.getName());
      }
    }

    @Nested
    class FlashCardGroupTest {
      @Test
      void whenFlashCardGroupsNotNull_thenObjectCreated() {
        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(null, flashCardGroup, null, null);

        // Validation
        assertNotNull(result);
      }

      @Test
      void whenNullFlashCardGroup_thenNullMappedInResult() {
        // Execution
        Quiz result = quizMapperImpl.internalToExternalQuizMapping(name, null, uuid, dueDateTime);

        // Validation
        assertNotNull(result);
        assertNull(result.getPrompts());
      }

      @Test
      void whenNullPrompts_thenNullMappedInResult() {
        // Setup
        flashCardGroup.setPrompts(null);

        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(name, flashCardGroup, uuid, dueDateTime);

        // Validation
        assertNotNull(result);
        assertNull(result.getPrompts());
      }

      @Test
      void whenFlashCardGroupPopulated_thenSameReturned() {
        // Setup
        assertNotNull(flashCardGroup.getPrompts(), "prompts");

        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(name, flashCardGroup, uuid, dueDateTime);

        // Validation
        assertNotNull(result);
        assertAll(
            () -> assertNotSame(flashCardGroup.getPrompts(), result.getPrompts(), "prompts"),
            () -> assertEquals(flashCardGroup.getPrompts(), result.getPrompts(), "prompts"),
            () -> assertEquals(name, result.getName(), "name"),
            () -> assertEquals(uuid, result.getId(), "id"),
            () -> assertEquals(dueDateTime, result.getDueDateTime(), "dueDateTime"));
      }
    }

    @Nested
    class UUIDTest {
      @Test
      void whenUuidNotNull_thenObjectCreated() {
        // Execution
        Quiz result = quizMapperImpl.internalToExternalQuizMapping(null, null, uuid, null);

        // Validation
        assertNotNull(result);
        assertEquals(uuid, result.getId());
      }

      @Test
      void whenNullUUID_thenNullMappedInResult() {
        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(name, flashCardGroup, null, dueDateTime);

        // Validation
        assertNotNull(result);
        assertNull(result.getId());
      }

      @Test
      void whenPopulatedUUID_thenNullMappedInResult() {
        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(name, flashCardGroup, uuid, dueDateTime);

        // Validation
        assertNotNull(result);
        assertSame(uuid, result.getId());
      }
    }

    @Nested
    class DueDateTimeTest {
      @Test
      void whenDueDateTimeNotNull_thenObjectCreated() {
        // Execution
        Quiz result = quizMapperImpl.internalToExternalQuizMapping(null, null, null, dueDateTime);

        // Validation
        assertNotNull(result);
        assertEquals(dueDateTime, result.getDueDateTime());
      }

      @Test
      void whenNullDueDateTime_thenNullMappedInResult() {
        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(name, flashCardGroup, uuid, null);

        // Validation
        assertNotNull(result);
        assertNull(result.getDueDateTime());
      }

      @Test
      void whenPopulatedDueDateTime_thenNullMappedInResult() {
        // Execution
        Quiz result =
            quizMapperImpl.internalToExternalQuizMapping(name, flashCardGroup, uuid, dueDateTime);

        // Validation
        assertNotNull(result);
        assertSame(dueDateTime, result.getDueDateTime());
      }
    }
  }
}
