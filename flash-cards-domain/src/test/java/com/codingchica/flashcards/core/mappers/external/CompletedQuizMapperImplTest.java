package com.codingchica.flashcards.core.mappers.external;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.core.model.external.QuizResult;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CompletedQuizMapperImplTest {
  private UUID uuid = UUID.randomUUID();
  private String name = "Quiz Name";
  private List<Map.Entry<String, String>> prompts = new ArrayList<>();
  private List<String> answers = new ArrayList<>();
  private Instant createdDateTime = Instant.now().minus(Duration.ofMinutes(1));
  private CompletedQuizMapper completedQuizMapper = new CompletedQuizMapperImpl();
  private CompletedQuiz.Builder completedQuizBuilder = CompletedQuiz.builder().answers(answers);
  private CompletedQuiz completedQuiz = spy(completedQuizBuilder.build());
  private Quiz.Builder quizBuilder =
      Quiz.builder().name(name).prompts(prompts).createdDateTime(createdDateTime).id(uuid);
  private Quiz quiz = spy(quizBuilder.build());

  @Nested
  class MapCompletedQuizToExternalResultsTest {
    @Test
    void whenQuizNull_thenExceptionThrown() {
      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToExternalResults(null, completedQuiz);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("quiz is marked non-null but is null", exception.getMessage());
    }

    @Test
    void whenCompletedQuizNull_thenExceptionThrown() {
      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToExternalResults(quiz, null);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("completedQuiz is marked non-null but is null", exception.getMessage());
    }

    @Test
    void whenQuizGetPromptsNull_thenExceptionThrown() {
      // Setup
      doReturn(null).when(quiz).getPrompts();

      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("quiz.getPrompts() must not be null", exception.getMessage());
    }

    @Test
    void whenQuizGetPromptsEmpty_thenProcessedSuccessfully() {
      // Setup
      doReturn(new ArrayList<String>()).when(quiz).getPrompts();

      // Execution
      QuizResult result =
          completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(name, result.getName(), "name"),
          () -> assertEquals(0, result.getPercentage(), "percentage"),
          () -> assertEquals(0, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(0, result.getPromptCount(), "promptCount"),
          () -> assertEquals(1, result.getTimeMinutes(), "timeMinutes"),
          () -> assertEquals(0, result.getTimeSeconds(), "timeSeconds"));
    }

    @Test
    void whenQuizGetPromptsContainsNull_thenProcessedSuccessfully() {
      // Setup
      prompts.add(null);
      answers.add(null);

      // Execution
      QuizResult result =
          completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(name, result.getName(), "name"),
          () -> assertEquals(0, result.getPercentage(), "percentage"),
          () -> assertEquals(0, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(0, result.getPromptCount(), "promptCount"),
          () -> assertEquals(1, result.getTimeMinutes(), "timeMinutes"),
          () -> assertEquals(0, result.getTimeSeconds(), "timeSeconds"));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "name GOES Here"})
    @NullAndEmptySource
    void whenNameVaried_thenProcessedSuccessfully(String nameValue) {
      // Setup
      quiz.setName(nameValue);

      // Execution
      QuizResult result =
          completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      assertEquals(nameValue, result.getName(), "name");
    }

    @Test
    void whenQuizGetCreatedDateTimeNull_thenExceptionThrown() {
      // Setup
      doReturn(null).when(quiz).getCreatedDateTime();

      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("quiz.getCreatedDateTime() must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void whenQuizGetCreatedDateTimeVaried_thenSuccessfullyProcessed(int minutesChange) {
      // Setup
      createdDateTime = Instant.now().minus(Duration.ofMinutes(minutesChange));
      quiz.setCreatedDateTime(createdDateTime);

      // Execution
      QuizResult result =
          completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      assertEquals(minutesChange, result.getTimeMinutes(), "timeMinutes");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void whenAllCorrectWithVariedPromptCounts_thenSuccessfullyProcessed(int promptCount) {
      // Setup
      IntStream.range(0, promptCount)
          .sequential()
          .forEach(
              count -> {
                Map.Entry<String, String> entry = Map.entry("key" + count, "value" + count);
                prompts.add(entry);
                answers.add(entry.getValue());
              });

      // Execution
      QuizResult result =
          completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(100, result.getPercentage(), "percentage"),
          () -> assertEquals(promptCount, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(promptCount, result.getPromptCount(), "promptCount"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void whenOnePromptCorrect_thenSuccessfullyProcessed(int incorrectPromptCount) {
      // Setup
      int correctPromptCount = 1;
      int promptCount = incorrectPromptCount + correctPromptCount;
      int expectedPercentage = 100 * correctPromptCount / promptCount;
      IntStream.range(0, incorrectPromptCount)
          .sequential()
          .forEach(
              count -> {
                Map.Entry<String, String> entry = Map.entry("key" + count, "value" + count);
                prompts.add(entry);
                answers.add("incorrect");
              });
      Map.Entry<String, String> entry = Map.entry("key", "value");
      prompts.add(entry);
      answers.add(entry.getValue());

      // Execution
      QuizResult result =
          completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(expectedPercentage, result.getPercentage(), "percentage"),
          () -> assertEquals(correctPromptCount, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(promptCount, result.getPromptCount(), "promptCount"));
    }

    @Test
    void whenQuizGetPromptsAndAnswersSizeDiffers_thenProcessedSuccessfully() {
      // Setup
      prompts.add(null);

      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);

      // Validation
      Exception exception = assertThrows(IllegalArgumentException.class, executable);
      assertEquals("prompts (1) and answers (0) size must be equivalent", exception.getMessage());
    }
  }

  @Nested
  class MapCompletedQuizToInternalResultsTest {
    // TODO add id comparisons
    @Test
    void whenQuizNull_thenExceptionThrown() {
      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToInternalResults(null, completedQuiz);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("quiz is marked non-null but is null", exception.getMessage());
    }

    @Test
    void whenCompletedQuizNull_thenExceptionThrown() {
      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToInternalResults(quiz, null);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("completedQuiz is marked non-null but is null", exception.getMessage());
    }

    @Test
    void whenQuizGetPromptsNull_thenExceptionThrown() {
      // Setup
      doReturn(null).when(quiz).getPrompts();

      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("quiz.getPrompts() must not be null", exception.getMessage());
    }

    @Test
    void whenQuizGetPromptsEmpty_thenProcessedSuccessfully() {
      // Setup
      doReturn(new ArrayList<String>()).when(quiz).getPrompts();

      // Execution
      com.codingchica.flashcards.core.model.internal.QuizResult result =
          completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(uuid, result.getId(), "id"),
          () -> assertEquals(name, result.getName(), "name"),
          () -> assertEquals(0, result.getPercentage(), "percentage"),
          () -> assertEquals(0, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(0, result.getPromptCount(), "promptCount"),
          () -> assertEquals(1, result.getTimeMinutes(), "timeMinutes"),
          () -> assertEquals(0, result.getTimeSeconds(), "timeSeconds"));
    }

    @Test
    void whenQuizGetPromptsContainsNull_thenProcessedSuccessfully() {
      // Setup
      prompts.add(null);
      answers.add(null);

      // Execution
      com.codingchica.flashcards.core.model.internal.QuizResult result =
          completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(uuid, result.getId(), "id"),
          () -> assertEquals(name, result.getName(), "name"),
          () -> assertEquals(0, result.getPercentage(), "percentage"),
          () -> assertEquals(0, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(0, result.getPromptCount(), "promptCount"),
          () -> assertEquals(1, result.getTimeMinutes(), "timeMinutes"),
          () -> assertEquals(0, result.getTimeSeconds(), "timeSeconds"));
    }

    @Test
    void whenQuizGetPromptsAndAnswersSizeDiffers_thenProcessedSuccessfully() {
      // Setup
      prompts.add(null);

      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      Exception exception = assertThrows(IllegalArgumentException.class, executable);
      assertEquals("prompts (1) and answers (0) size must be equivalent", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "name GOES Here"})
    @NullAndEmptySource
    void whenNameVaried_thenProcessedSuccessfully(String nameValue) {
      // Setup
      quiz.setName(nameValue);

      // Execution
      com.codingchica.flashcards.core.model.internal.QuizResult result =
          completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      assertEquals(nameValue, result.getName(), "name");
    }

    @Test
    void whenQuizGetCreatedDateTimeNull_thenExceptionThrown() {
      // Setup
      doReturn(null).when(quiz).getCreatedDateTime();

      // Execution
      Executable executable =
          () -> completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("quiz.getCreatedDateTime() must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void whenQuizGetCreatedDateTimeVaried_thenSuccessfullyProcessed(int minutesChange) {
      // Setup
      createdDateTime = Instant.now().minus(Duration.ofMinutes(minutesChange));
      quiz.setCreatedDateTime(createdDateTime);

      // Execution
      com.codingchica.flashcards.core.model.internal.QuizResult result =
          completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      assertEquals(minutesChange, result.getTimeMinutes(), "timeMinutes");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void whenAllCorrectWithVariedPromptCounts_thenSuccessfullyProcessed(int promptCount) {
      // Setup
      IntStream.range(0, promptCount)
          .sequential()
          .forEach(
              count -> {
                Map.Entry<String, String> entry = Map.entry("key" + count, "value" + count);
                prompts.add(entry);
                answers.add(entry.getValue());
              });

      // Execution
      com.codingchica.flashcards.core.model.internal.QuizResult result =
          completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(100, result.getPercentage(), "percentage"),
          () -> assertEquals(promptCount, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(promptCount, result.getPromptCount(), "promptCount"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void whenOnePromptCorrect_thenSuccessfullyProcessed(int incorrectPromptCount) {
      // Setup
      int correctPromptCount = 1;
      int promptCount = incorrectPromptCount + correctPromptCount;
      int expectedPercentage = 100 * correctPromptCount / promptCount;
      IntStream.range(0, incorrectPromptCount)
          .sequential()
          .forEach(
              count -> {
                Map.Entry<String, String> entry = Map.entry("key" + count, "value" + count);
                prompts.add(entry);
                answers.add("incorrect");
              });
      Map.Entry<String, String> entry = Map.entry("key", "value");
      prompts.add(entry);
      answers.add(entry.getValue());

      // Execution
      com.codingchica.flashcards.core.model.internal.QuizResult result =
          completedQuizMapper.mapCompletedQuizToInternalResults(quiz, completedQuiz);

      // Validation
      assertAll(
          () -> assertEquals(expectedPercentage, result.getPercentage(), "percentage"),
          () -> assertEquals(correctPromptCount, result.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(promptCount, result.getPromptCount(), "promptCount"));
    }
  }
}
