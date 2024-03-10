package com.codingchica.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.codingchica.flashcards.core.mappers.QuizMapper;
import com.codingchica.flashcards.core.mappers.QuizMapperImpl;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

class QuizServiceTest {
  private QuizMapper quizMapper = spy(new QuizMapperImpl());
  private FlashCardsConfiguration flashCardsConfiguration = mock(FlashCardsConfiguration.class);
  private String flashCardGroupKey1 = "some key value here";
  private FlashCardGroup flashCardGroupValue1 = mock(FlashCardGroup.class);
  private String flashCardGroupKey2 = "some OTHER key value here";
  private FlashCardGroup flashCardGroupValue2 = mock(FlashCardGroup.class);
  private Map<String, FlashCardGroup> flashCardGroupMap = new HashMap<>();
  private UUID uuid = UUID.randomUUID();
  private ZonedDateTime dueDateTime = ZonedDateTime.now().plus(Duration.ofSeconds(60));
  private QuizService.Builder quizServiceBuilder =
      QuizService.builder()
          .quizMapper(quizMapper)
          .flashCardsConfiguration(flashCardsConfiguration)
          .uuidGenerator(new ObjectIdGenerators.UUIDGenerator());

  private QuizService quizService = quizServiceBuilder.build();

  @BeforeEach
  public void setup() {
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
        quizServiceBuilder = QuizService.builder().quizMapper(quizMapper);

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

    @Nested
    class UUIDGeneratorTest {

      @Test
      void build_whenUUIDGeneratorNotInvoked_thenExceptionThrown() {
        // Setup
        quizServiceBuilder =
            QuizService.builder()
                .flashCardsConfiguration(flashCardsConfiguration)
                .quizMapper(quizMapper);

        // Execution
        Executable executable = () -> quizServiceBuilder.build();

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("uuidGenerator is marked non-null but is null", exception.getMessage());
      }

      @Test
      void builderSetter_whenUuidGeneratorNull_thenExceptionThrown() {
        // Execution
        Executable executable = () -> quizServiceBuilder.uuidGenerator(null);

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("uuidGenerator is marked non-null but is null", exception.getMessage());
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
      assertEquals(
          "QuizService.Builder(flashCardsConfiguration=null, quizMapper=null, uuidGenerator=null)",
          result);
    }
  }

  @Nested
  class ListQuizzesTest {
    @Test
    void listQuizzes_whenFlashCardsGroupMapNull_thenExceptionThrown() {
      // Setup
      doReturn(null).when(flashCardsConfiguration).getFlashCardGroupMap();

      // Execution
      Executable executable = () -> quizService.listQuizzes();

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("flashCardGroupMap must not be null", exception.getMessage());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapEmpty_thenEmptyListReturned() {
      // Setup
      flashCardGroupMap.clear();

      // Execution
      List<Quiz> result = quizService.listQuizzes();

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
      List<Quiz> result = quizService.listQuizzes();

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
      List<Quiz> result = quizService.listQuizzes();

      // Validation
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapContainsOneEntry_thenMappedValueReturned() {
      // Setup
      assertEquals(1, flashCardGroupMap.size(), "flashCardGroupMap.size()" + flashCardGroupMap);
      assertSame(flashCardGroupValue1, flashCardGroupMap.get(flashCardGroupKey1));

      // Execution
      List<Quiz> quizList = quizService.listQuizzes();

      // Validation
      assertNotNull(quizList);
      assertFalse(quizList.isEmpty());
      assertEquals(1, quizList.size());

      Quiz quiz = quizList.get(0);
      assertNotNull(quiz);
      assertEquals(flashCardGroupKey1, quiz.getName());

      verify(quizMapper)
          .internalToExternalQuizMapping(
              eq(flashCardGroupKey1),
              eq(flashCardGroupValue1),
              any(UUID.class),
              any(ZonedDateTime.class));
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
      List<Quiz> quizList = quizService.listQuizzes();

      // Validation
      assertNotNull(quizList);
      assertFalse(quizList.isEmpty());
      assertEquals(expectedSize, quizList.size());

      verify(quizMapper)
          .internalToExternalQuizMapping(
              eq(flashCardGroupKey1),
              eq(flashCardGroupValue1),
              any(UUID.class),
              any(ZonedDateTime.class));
      verify(quizMapper)
          .internalToExternalQuizMapping(
              eq(flashCardGroupKey2),
              eq(flashCardGroupValue2),
              any(UUID.class),
              any(ZonedDateTime.class));
    }

    @Test
    void listQuizzes_whenCalledSecondTime_theSameDataReturned() {
      // Setup
      int expectedSize = 1;
      assertEquals(
          expectedSize, flashCardGroupMap.size(), "flashCardGroupMap.size()" + flashCardGroupMap);
      assertSame(flashCardGroupValue1, flashCardGroupMap.get(flashCardGroupKey1));
      List<Quiz> initialResult = quizService.listQuizzes();
      Mockito.reset(quizMapper);

      // Execution
      List<Quiz> quizList = quizService.listQuizzes();

      // Validation
      assertEquals(initialResult.get(0).getName(), quizList.get(0).getName());
      verify(quizMapper)
          .internalToExternalQuizMapping(
              eq(flashCardGroupKey1),
              eq(flashCardGroupValue1),
              any(UUID.class),
              any(ZonedDateTime.class));
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
      doReturn(null)
          .when(quizMapper)
          .internalToExternalQuizMapping(anyString(), any(), any(), any());

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

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupKey1);

      // Validation
      assertFalse(optionalQuiz.isEmpty());
      Quiz quiz = optionalQuiz.get();

      assertNotNull(quiz);
      assertEquals(flashCardGroupKey1, quiz.getName());
    }

    @Test
    void getQuiz_whenFlashCardsGroupMapContainsNullKey_thenIgnored() {
      // Setup
      flashCardGroupMap.put(null, flashCardGroupValue2);

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupKey1);

      // Validation
      assertNotNull(optionalQuiz);
      assertTrue(optionalQuiz.isPresent());
      assertEquals(flashCardGroupKey1, optionalQuiz.get().getName());
    }

    @Test
    void getQuiz_whenFlashCardsGroupMapContainsNullValue_thenIgnored() {
      // Setup
      flashCardGroupMap.put("some key value", null);

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupKey1);

      // Validation
      assertNotNull(optionalQuiz);
      assertTrue(optionalQuiz.isPresent());
      assertEquals(flashCardGroupKey1, optionalQuiz.get().getName());
    }
  }
}
