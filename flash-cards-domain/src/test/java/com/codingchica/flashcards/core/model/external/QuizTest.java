package com.codingchica.flashcards.core.model.external;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class QuizTest {
  private Quiz.Builder quizBuilder = Quiz.builder();
  private Quiz quiz = quizBuilder.build();

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
  }

  @Nested
  class DueDateTimeTest {
    @Test
    void whenSetInBuilder_thenSameReturnedInGetter() {
      // Setup
      Instant zonedDateTime = Instant.now();
      String timeString = zonedDateTime.toString();
      quizBuilder.dueDateTime(timeString);

      // Execution
      Quiz quiz = quizBuilder.build();

      // Validation
      assertNotNull(quiz);
      assertSame(timeString, quiz.getDueDateTime());
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
  }

  @Nested
  class BuilderTest {
    @Test
    void toString_whenInvoked_thenReturnsExpectedValue() {
      // Execution
      String result = quizBuilder.toString();

      // Validation
      assertEquals("Quiz.Builder(id=null, name=null, dueDateTime=null, prompts=null)", result);
    }
  }
}
