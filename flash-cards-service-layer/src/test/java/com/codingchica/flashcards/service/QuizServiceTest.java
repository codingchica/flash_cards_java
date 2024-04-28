package com.codingchica.flashcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.codingchica.flashcards.core.exceptions.RenderableException;
import com.codingchica.flashcards.core.mappers.external.CompletedQuizMapper;
import com.codingchica.flashcards.core.mappers.external.CompletedQuizMapperImpl;
import com.codingchica.flashcards.core.mappers.external.QuizMapper;
import com.codingchica.flashcards.core.mappers.external.QuizMapperImpl;
import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.core.model.external.QuizResult;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.IntStream;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class QuizServiceTest {
  private String flashCardGroupKey1 = "some key value here";
  private String flashCardGroupKey2 = "some OTHER key value here";
  private FlashCardsConfiguration flashCardsConfiguration = mock(FlashCardsConfiguration.class);
  private Map<String, List<FlashCardGroup>> flashCardGroupMap = new HashMap<>();
  private List<FlashCardGroup> flashCardGroupList = new ArrayList<>();
  private Map<String, String> prompts = new HashMap<>();
  private FlashCardGroup flashCardGroupValue1 =
      spy(FlashCardGroup.builder().prompts(prompts).name("name1").build());
  private FlashCardGroup flashCardGroupValue2 = mock(FlashCardGroup.class);
  private QuizMapper quizMapper = spy(QuizMapperImpl.builder().build());
  private Quiz quiz = null;
  private ObjectMapper objectMapper = spy(new ObjectMapper());
  private CompletedQuizMapper completedQuizMapper = spy(new CompletedQuizMapperImpl());
  private QuizService.Builder quizServiceBuilder =
      QuizService.builder()
          .quizMapper(quizMapper)
          .completedQuizMapper(completedQuizMapper)
          .flashCardsConfiguration(flashCardsConfiguration)
          .objectMapper(objectMapper);
  private List<String> submittedAnswers = new ArrayList<>();
  private CompletedQuiz.Builder completedQuizBuilder =
      CompletedQuiz.builder().answers(submittedAnswers);
  private CompletedQuiz completedQuiz = completedQuizBuilder.build();

  private QuizService quizService = quizServiceBuilder.build();
  private Cache<UUID, Quiz> quizCache = quizService.getQuizCache();
  private ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();
  private UUID uuid = uuidGenerator.generateId(String.class);
  private Random random = new Random();

  @BeforeEach
  public void setup() {
    prompts.put("promptKey1", "promptValue1");

    flashCardGroupList.add(flashCardGroupValue1);

    flashCardGroupMap.put(flashCardGroupKey1, flashCardGroupList);

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
    class CompletedQuizMapperTest {
      @Test
      void build_whenCompletedQuizMapperNotInvoked_thenExceptionThrown() {
        // Setup
        quizServiceBuilder =
            QuizService.builder()
                .quizMapper(quizMapper)
                .flashCardsConfiguration(flashCardsConfiguration)
                .objectMapper(objectMapper);

        // Execution
        Executable executable = () -> quizServiceBuilder.build();

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("completedQuizMapper is marked non-null but is null", exception.getMessage());
      }

      @Test
      void builderSetter_whenQuizMapperNull_thenExceptionThrown() {
        // Execution
        Executable executable = () -> quizServiceBuilder.completedQuizMapper(null);

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("completedQuizMapper is marked non-null but is null", exception.getMessage());
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
    class ObjectMapperTest {
      @Test
      void build_whenObjectMapperNotInvoked_thenExceptionThrown() {
        // Setup
        quizServiceBuilder =
            QuizService.builder()
                .flashCardsConfiguration(flashCardsConfiguration)
                .completedQuizMapper(completedQuizMapper)
                .quizMapper(quizMapper);

        // Execution
        Executable executable = () -> quizServiceBuilder.build();

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("objectMapper is marked non-null but is null", exception.getMessage());
      }

      @Test
      void builderSetter_whenObjectMapperNull_thenExceptionThrown() {
        // Execution
        Executable executable = () -> quizServiceBuilder.objectMapper(null);

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("objectMapper is marked non-null but is null", exception.getMessage());
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
          "QuizService.Builder(flashCardsConfiguration=null, quizMapper=null,"
              + " completedQuizMapper=null, objectMapper=null)",
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
      Executable executable = () -> quizService.listQuizNamesByCategory();

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("flashCardGroupMap must not be null", exception.getMessage());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapEmpty_thenEmptyListReturned() {
      // Setup
      flashCardGroupMap.clear();

      // Execution
      Map<String, List<String>> result = quizService.listQuizNamesByCategory();

      // Validation
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapContainsNullKey_thenIgnored() {
      // Setup
      flashCardGroupMap.clear();
      flashCardGroupMap.put(null, flashCardGroupList);

      // Execution
      Map<String, List<String>> result = quizService.listQuizNamesByCategory();

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
      Map<String, List<String>> result = quizService.listQuizNamesByCategory();

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
      assertSame(flashCardGroupList, flashCardGroupMap.get(flashCardGroupKey1));
      List<String> expectedResult = new ArrayList<>();
      expectedResult.add(flashCardGroupValue1.getName());

      // Execution
      Map<String, List<String>> quizList = quizService.listQuizNamesByCategory();

      // Validation
      assertNotNull(quizList);
      assertFalse(quizList.isEmpty());
      assertEquals(1, quizList.size());

      Map.Entry<String, List<String>> quizEntry = quizList.entrySet().stream().findFirst().get();
      assertEquals(flashCardGroupKey1, quizEntry.getKey());
      assertEquals(expectedResult, quizEntry.getValue());
    }

    @Test
    void listQuizzes_whenFlashCardsGroupMapContainsTwoEntries_thenBothReturned() {
      // Setup
      int expectedSize = 2;
      List<FlashCardGroup> flashCardGroupList2 = new ArrayList<>();
      flashCardGroupList2.add(flashCardGroupValue2);
      flashCardGroupMap.put(flashCardGroupKey2, flashCardGroupList2);
      assertEquals(
          expectedSize, flashCardGroupMap.size(), "flashCardGroupMap.size()" + flashCardGroupMap);
      assertSame(flashCardGroupList, flashCardGroupMap.get(flashCardGroupKey1));

      // Execution
      Map<String, List<String>> quizList = quizService.listQuizNamesByCategory();

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
      assertSame(flashCardGroupList, flashCardGroupMap.get(flashCardGroupKey1));
      Map<String, List<String>> initialResult = quizService.listQuizNamesByCategory();

      // Execution
      Map<String, List<String>> quizList = quizService.listQuizNamesByCategory();

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
      doReturn(null).when(quizMapper).internalToExternalQuizMapping(any());

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
      System.out.println(flashCardGroupKey1);

      // Execution
      Optional<Quiz> optionalQuiz = quizService.getQuiz(flashCardGroupValue1.getName());

      // Validation
      System.out.println(optionalQuiz);
      assertFalse(optionalQuiz.isEmpty());
      Quiz quiz = optionalQuiz.get();

      assertNotNull(quiz);
      assertEquals(flashCardGroupValue1.getName(), quiz.getName());
      verify(quizMapper).internalToExternalQuizMapping(flashCardGroupValue1);
      verifyNoMoreInteractions(quizMapper);
    }

    @Test
    void getQuiz_whenFlashCardsGroupMapContainsNullKey_thenIgnored() {
      // Setup
      flashCardGroupMap.clear();
      List<FlashCardGroup> flashCardGroupList2 = new ArrayList<>();
      flashCardGroupList2.add(flashCardGroupValue2);
      flashCardGroupMap.put(null, flashCardGroupList2);

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

  @Nested
  class GradeQuizTest {
    @Test
    void gradeQuiz_whenIdNull_thenThrowsException() {
      // Execution
      Executable executable = () -> quizService.gradeQuiz(null, completedQuiz);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("id is marked non-null but is null", exception.getMessage());
    }

    @Test
    void gradeQuiz_whenCompletedQuizNull_thenThrowsException() {
      // Execution
      Executable executable = () -> quizService.gradeQuiz(uuid, null);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("completedQuiz is marked non-null but is null", exception.getMessage());
    }

    @Test
    void gradeQuiz_whenQuizNotFoundInCache_thenThrowsException() {
      // Execution
      Executable executable = () -> quizService.gradeQuiz(uuid, completedQuiz);

      // Validation
      RenderableException exception = assertThrows(RenderableException.class, executable);
      assertEquals(String.format("Quiz='%s' not found", uuid), exception.getMessage());
      assertEquals(HttpStatus.NOT_FOUND_404, exception.getHttpStatus());
    }

    @Test
    void gradeQuiz_whenQuizNameMismatch_thenThrowsException() {
      // Setup
      int promptCount = 10;
      prompts.clear();
      IntStream.range(0, promptCount)
          .forEachOrdered(
              count -> {
                int value = random.nextInt();
                prompts.put("key" + count, String.valueOf(value));
              });
      quiz = quizMapper.internalToExternalQuizMapping(flashCardGroupValue1);
      quizCache.put(uuid, quiz);
      completedQuiz.setName("mismatching name");

      // Execution
      Executable executable = () -> quizService.gradeQuiz(uuid, completedQuiz);

      // Validation
      RenderableException exception = assertThrows(RenderableException.class, executable);
      assertEquals(String.format("Quiz name mismatch", uuid), exception.getMessage());
      assertEquals(HttpStatus.NOT_FOUND_404, exception.getHttpStatus());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void gradeQuiz_whenQuizInCache_thenExpectedResultReturned(int correctAnswers)
        throws RenderableException, IOException {
      // Setup
      int promptCount = 10;
      int expectedPercentage = correctAnswers * 100 / promptCount;
      prompts.clear();
      IntStream.range(0, promptCount)
          .forEachOrdered(
              count -> {
                int value = random.nextInt();
                prompts.put("key" + count, String.valueOf(value));
              });
      quiz = quizMapper.internalToExternalQuizMapping(flashCardGroupValue1);
      quizCache.put(uuid, quiz);
      completedQuiz.setName(quiz.getName());
      int correctCount = 0;
      List<Map.Entry<String, String>> promptEntries = quiz.getPrompts();
      for (Map.Entry<String, String> prompt : promptEntries) {
        String value = prompt.getValue();
        if (correctCount < correctAnswers) {
          submittedAnswers.add(String.valueOf(value));
          correctCount++;
        } else {
          submittedAnswers.add(value + 1);
        }
      }

      // Execution
      QuizResult quizResult = quizService.gradeQuiz(uuid, completedQuiz);

      // Validation
      assertNotNull(quizResult);
      assertAll(
          () -> assertEquals("name1", quizResult.getName(), "name"),
          () -> assertEquals(promptCount, quizResult.getPromptCount(), "promptCount"),
          () -> assertEquals(correctAnswers, quizResult.getCorrectAnswers(), "correctAnswers"),
          () -> assertEquals(expectedPercentage, quizResult.getPercentage(), "percentage"));
    }
  }
}
