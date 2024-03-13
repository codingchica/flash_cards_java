package com.codingchica.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.codingchica.flashcards.core.mappers.QuizMapper;
import com.codingchica.flashcards.core.mappers.QuizMapperImpl;
import com.codingchica.flashcards.core.model.external.Quiz;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class QuizServiceTest {
  private String flashCardGroupKey1 = "some key value here";
  private String flashCardGroupKey2 = "some OTHER key value here";
  private FlashCardsConfiguration flashCardsConfiguration = mock(FlashCardsConfiguration.class);
  private Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
  private Map<String, String> prompts = new HashMap<>();
  private FlashCardGroup flashCardGroupValue1 =
      spy(FlashCardGroup.builder().prompts(prompts).build());
  private FlashCardGroup flashCardGroupValue2 = mock(FlashCardGroup.class);
  private QuizMapper quizMapper = spy(QuizMapperImpl.builder().build());
  private QuizService.Builder quizServiceBuilder =
      QuizService.builder().quizMapper(quizMapper).flashCardsConfiguration(flashCardsConfiguration);

  private QuizService quizService = quizServiceBuilder.build();

  @BeforeEach
  public void setup() {
    prompts.put("promptKey1", "promptValue1");

    flashCardGroupMap.put(flashCardGroupKey1, flashCardGroupValue1);

    doReturn(flashCardGroupMap).when(flashCardsConfiguration).getFlashCardGroupMap();
  }

  @Nested
  class BuilderTest {
    @Nested
    class FlashCardsConfigurationTest {
      @Test
      void builderSetter_whenFlashCardsConfigurationNull_thenExceptionThrown() {
        // Execution
        Executable executable = () -> quizServiceBuilder.flashCardsConfiguration(null);

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals(
            "flashCardsConfiguration is marked non-null but is null", exception.getMessage());
      }

      @Test
      void build_whenFlashCardsConfigurationNotInvoked_thenExceptionThrown() {
        // Setup
        quizServiceBuilder = QuizService.builder();

        // Execution
        Executable executable = () -> quizServiceBuilder.build();

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals(
            "flashCardsConfiguration is marked non-null but is null", exception.getMessage());
      }
    }

    @Nested
    class QuizMapperTest {
      @Test
      void build_whenQuizMapperNotInvoked_thenExceptionThrown() {
        // Setup
        quizServiceBuilder = QuizService.builder().flashCardsConfiguration(flashCardsConfiguration);

        // Execution
        Executable executable = () -> quizServiceBuilder.build();

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("quizMapper is marked non-null but is null", exception.getMessage());
      }

      @Test
      void builderSetter_whenQuizMapperNull_thenExceptionThrown() {
        // Execution
        Executable executable = () -> quizServiceBuilder.quizMapper(null);

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("quizMapper is marked non-null but is null", exception.getMessage());
      }
    }

    @Test
    void build_whenInvoked_thenExpectedObjectReturned() {
      // Execution
      QuizService quizService = quizServiceBuilder.build();

      // Validation
      assertNotNull(quizService);
      // No getters exposed to test
    }

    @Test
    void toString_whenInvoked_thenReturnsExpectedValue() {
      // Setup
      quizServiceBuilder = QuizService.builder();

      // Execution
      String result = quizServiceBuilder.toString();

      // Validation
      assertEquals("QuizService.Builder(flashCardsConfiguration=null, quizMapper=null)", result);
    }
  }

  @Nested
  class ListQuizzesTest {
    @Test
    void listQuizzes_whenFlashCardsGroupMapNull_thenExceptionThrown() {
      // Setup
      doReturn(null).when(flashCardsConfiguration).getFlashCardGroupMap();

      // Execution
      Executable executable = () -> quizService.listQuizNames();

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("flashCardGroupMap must not be null", exception.getMessage());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapEmpty_thenEmptyListReturned() {
      // Setup
      flashCardGroupMap.clear();

      // Execution
      List<String> result = quizService.listQuizNames();

      // Validation
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapContainsNullKey_thenIgnored() {
      // Setup
      flashCardGroupMap.clear();
      flashCardGroupMap.put(null, flashCardGroupValue1);

      // Execution
      List<String> result = quizService.listQuizNames();

      // Validation
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapContainsNullValue_thenIgnored() {
      // Setup
      flashCardGroupMap.clear();
      flashCardGroupMap.put("some key value", null);

      // Execution
      List<String> result = quizService.listQuizNames();

      // Validation
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapContainsOneEntry_thenMappedValueReturned() {
      // Setup
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime upperLimit = ZonedDateTime.now().plus(Duration.ofMinutes(1));
      assertEquals(1, flashCardGroupMap.size(), "flashCardGroupMap.size()" + flashCardGroupMap);
      assertSame(flashCardGroupValue1, flashCardGroupMap.get(flashCardGroupKey1));

      // Execution
      List<String> quizList = quizService.listQuizNames();

      // Validation
      assertNotNull(quizList);
      assertFalse(quizList.isEmpty());
      assertEquals(1, quizList.size());

      String quiz = quizList.get(0);
      assertEquals(flashCardGroupKey1, quiz);
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapContainsTwoEntries_thenBothReturned() {
      // Setup
      int expectedSize = 2;
      flashCardGroupMap.put(flashCardGroupKey2, flashCardGroupValue2);
      assertEquals(
          expectedSize, flashCardGroupMap.size(), "flashCardGroupMap.size()" + flashCardGroupMap);
      assertSame(flashCardGroupValue1, flashCardGroupMap.get(flashCardGroupKey1));

      // Execution
      List<String> quizList = quizService.listQuizNames();

      // Validation
      assertNotNull(quizList);
      assertFalse(quizList.isEmpty());
      assertEquals(expectedSize, quizList.size());

      verifyNoInteractions(quizMapper);
    }

    @Test
    void listQuizzes_whenCalledSecondTime_theSameDataReturned() {
      // Setup
      int expectedSize = 1;
      assertEquals(
          expectedSize, flashCardGroupMap.size(), "flashCardGroupMap.size()" + flashCardGroupMap);
      assertSame(flashCardGroupValue1, flashCardGroupMap.get(flashCardGroupKey1));
      List<String> initialResult = quizService.listQuizNames();

      // Execution
      List<String> quizList = quizService.listQuizNames();

      // Validation
      assertEquals(initialResult.get(0), quizList.get(0));
      verifyNoInteractions(quizMapper);
    }
  }

  @Nested
  class GetQuizTest {
    @Test
    void getQuiz_whenQuizNameNull_throwsException() {
      // Setup

      // Execution
      Executable executable = () -> quizService.getQuiz(null);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("quizName is marked non-null but is null", exception.getMessage());
    }

    @Test
    void getQuiz_whenQuizNull_thenIgnored() {
      // Setup
      doReturn(null).when(quizMapper).internalToExternalQuizMapping(anyString(), any());

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupKey1);

      // Validation
      assertTrue(optionalQuiz.isEmpty());
    }

    @Test
    void getQuiz_whenQuizNameMismatch_thenNoResultReturned() {
      // Setup

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz("not a quiz name match");

      // Validation
      assertTrue(optionalQuiz.isEmpty());
    }

    @Test
    void getQuiz_whenQuizNameMatch_thenReturned() {
      // Setup
      assertFalse(flashCardGroupValue1.getPrompts().isEmpty());

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupKey1);

      // Validation
      assertFalse(optionalQuiz.isEmpty());
      Quiz quiz = optionalQuiz.get();

      assertNotNull(quiz);
      assertEquals(flashCardGroupKey1, quiz.getName());
      verify(quizMapper).internalToExternalQuizMapping(flashCardGroupKey1, flashCardGroupValue1);
      verifyNoMoreInteractions(quizMapper);
    }

    @Test
    void getQuiz_whenFlashCardsGroupMapContainsNullKey_thenIgnored() {
      // Setup
      flashCardGroupMap.clear();
      flashCardGroupMap.put(null, flashCardGroupValue2);

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupKey1);

      // Validation
      assertNotNull(optionalQuiz);
      assertTrue(optionalQuiz.isEmpty());
    }

    @Test
    void getQuiz_whenFlashCardsGroupMapContainsNullValue_thenIgnored() {
      // Setup
      flashCardGroupMap.clear();
      flashCardGroupMap.put("some key value", null);

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupKey1);

      // Validation
      assertNotNull(optionalQuiz);
      assertTrue(optionalQuiz.isEmpty());
    }
  }
}
