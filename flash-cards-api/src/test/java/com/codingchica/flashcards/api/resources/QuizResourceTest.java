package com.codingchica.flashcards.api.resources;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.codingchica.flashcards.core.exceptions.RenderableException;
import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.core.model.external.QuizResult;
import com.codingchica.flashcards.service.QuizService;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizResourceTest {
  private List<Quiz> quizzes = new ArrayList<>();
  private Quiz.Builder quizBuilder = Quiz.builder().name("Quiz name");
  private Quiz quiz = quizBuilder.build();
  private CompletedQuiz.Builder completedQuizBuilder = CompletedQuiz.builder();
  private CompletedQuiz completedQuiz = completedQuizBuilder.build();
  private QuizResult quizResult = QuizResult.builder().build();
  @Mock private QuizService quizService;
  private QuizResource.Builder quizResourceBuilder = QuizResource.builder();
  private QuizResource quizResource;

  @BeforeEach
  void setup() {
    quizResourceBuilder.quizService(quizService);
    quizResource = quizResourceBuilder.build();
  }

  @Nested
  class ListQuizzesTest {
    @Test
    void whenQuizzesNull_thenEmptyListReturned() {
      // Setup
      doReturn(null).when(quizService).listQuizNamesByCategory();

      // Execution
      Map<String, List<String>> quizNames = quizResource.listQuizzes();

      // Validation
      assertNull(quizNames);
    }

    @Test
    void whenQuizzesEmpty_thenEmptyListReturned() {
      // Setup
      doReturn(Collections.EMPTY_MAP).when(quizService).listQuizNamesByCategory();

      // Execution
      Map<String, List<String>> quizNames = quizResource.listQuizzes();

      // Validation
      assertEquals(Collections.EMPTY_MAP, quizNames);
    }

    @Test
    void whenQuizNamePopulated_thenReturned() {
      // Setup
      String name1 = quiz.getName();
      String name2 = "quizName2";
      String[] expectedNames = {name1, name2};
      Map<String, List<String>> expectedNamesMap = new HashMap<>();
      doReturn(expectedNamesMap).when(quizService).listQuizNamesByCategory();

      // Execution
      Map<String, List<String>> quizNames = quizResource.listQuizzes();

      // Validation
      assertEquals(expectedNamesMap, quizNames);
    }
  }

  @Nested
  class GetQuizTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void whenQuizNameVaried_thenSamePassedToQuizService(String quizName)
        throws RenderableException {
      // Setup
      doReturn(Optional.of(quiz)).when(quizService).getQuiz(quizName);

      // Execution
      Quiz result = quizResource.getQuiz(quizName);

      // Validation
      verify(quizService).getQuiz(quizName);
      assertSame(quiz, result);
    }

    @Test
    void whenQuizNameNotFound_thenExceptionThrown() {
      // Setup
      doReturn(Optional.empty()).when(quizService).getQuiz(quiz.getName());

      // Execution
      Executable executable = () -> quizResource.getQuiz(quiz.getName());

      // Validation
      RenderableException exception = assertThrows(RenderableException.class, executable);
      assertEquals(
          String.format("No match found for quiz: '%s'", quiz.getName()), exception.getMessage());
    }
  }

  @Nested
  class GradeQuizTest {

    @Test
    void whenInvoked_thenSamePassedToQuizService() throws RenderableException, IOException {
      // Setup
      UUID uuid = UUID.randomUUID();
      doReturn(quizResult).when(quizService).gradeQuiz(uuid, completedQuiz);

      // Execution
      QuizResult result = quizResource.gradeQuiz(uuid, completedQuiz);

      // Validation
      assertSame(quizResult, result);
      verify(quizService).gradeQuiz(eq(uuid), eq(completedQuiz));
    }
  }

  @Nested
  class BuilderTest {
    @Nested
    class QuizServiceTest {
      @Test
      void whenQuizServiceSetNull_thenExceptionThrown() {
        // Setup

        // Execution
        Executable executable = () -> quizResourceBuilder.quizService(null);

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("quizService is marked non-null but is null", exception.getMessage());
      }

      @Test
      void whenQuizServiceDefault_thenExceptionThrown() {
        // Setup
        quizResourceBuilder = QuizResource.builder();

        // Execution
        Executable executable = () -> quizResourceBuilder.build();

        // Validation
        Exception exception = assertThrows(NullPointerException.class, executable);
        assertEquals("quizService is marked non-null but is null", exception.getMessage());
      }

      @Test
      void whenQuizServicePopulated_thenReturnedInGetter() {
        // Setup
        quizResourceBuilder.quizService(quizService);

        // Execution
        QuizResource quizResource = quizResourceBuilder.build();

        // Validation
        assertSame(quizService, quizResource.getQuizService());
      }
    }

    @Nested
    class ToStringTest {
      @Test
      void whenInvoked_thenReturnsExpectedValue() {
        // Setup

        // Execution
        String result = QuizResource.builder().toString();

        // Validation
        assertEquals("QuizResource.Builder(quizService=null)", result);
      }
    }
  }
}
