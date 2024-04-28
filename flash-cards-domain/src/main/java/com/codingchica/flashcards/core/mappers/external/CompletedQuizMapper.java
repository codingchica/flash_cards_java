package com.codingchica.flashcards.core.mappers.external;

import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.core.model.external.QuizResult;
import lombok.NonNull;

/**
 * An interface to the mapping of completed quiz information to an internal representation of that
 * information.
 */
public interface CompletedQuizMapper {

  /**
   * Map the quiz information to an external quiz result.
   *
   * @param quiz The quiz which was presented to the user.
   * @param completedQuiz The completed quiz that was returned to the server.
   * @return A QuizResult to represent a graded quiz externally.
   */
  QuizResult mapCompletedQuizToExternalResults(
      @NonNull Quiz quiz, @NonNull CompletedQuiz completedQuiz);

  /**
   * Map the quiz information to an internal quiz result.
   *
   * @param quiz The quiz which was presented to the user.
   * @param completedQuiz The completed quiz that was returned to the server.
   * @return A QuizResult to represent a graded quiz internally.
   */
  com.codingchica.flashcards.core.model.internal.QuizResult mapCompletedQuizToInternalResults(
      @NonNull Quiz quiz, @NonNull CompletedQuiz completedQuiz);
}
