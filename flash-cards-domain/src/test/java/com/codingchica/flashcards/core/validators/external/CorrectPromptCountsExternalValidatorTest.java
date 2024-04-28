package com.codingchica.flashcards.core.validators.external;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.codingchica.flashcards.core.model.external.QuizResult;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CorrectPromptCountsExternalValidatorTest {
  private QuizResult.Builder quizResultBuilder = QuizResult.builder();

  private QuizResult quizResult = quizResultBuilder.build();

  private CorrectPromptCountsExternalValidator correctPromptCountsExternalValidator =
      new CorrectPromptCountsExternalValidator();
  private ConstraintValidatorContext constraintValidatorContext =
      mock(ConstraintValidatorContext.class);

  @Nested
  class IsValidTest {
    @Test
    void whenFlashCardGroupNull_thenValid() {
      // Execution
      boolean result =
          correctPromptCountsExternalValidator.isValid(null, constraintValidatorContext);

      // Validation
      assertTrue(result);
      verifyNoInteractions(constraintValidatorContext);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 5, 10})
    void whenCorrectAnswersLessThanOrEqualToPromptCount_thenValid(int correctAnswers) {
      // Setup
      int promptCount = 10;
      quizResult =
          quizResultBuilder.correctAnswers(correctAnswers).promptCount(promptCount).build();

      // Execution
      boolean result =
          correctPromptCountsExternalValidator.isValid(quizResult, constraintValidatorContext);

      // Validation
      assertTrue(result);
      verifyNoInteractions(constraintValidatorContext);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 200, Integer.MAX_VALUE})
    void whenPromptCountGreaterThanOrEqualToCorrectAnswers_thenValid(int promptCount) {
      // Setup
      int correctAnswers = 5;
      quizResult =
          quizResultBuilder.correctAnswers(correctAnswers).promptCount(promptCount).build();

      // Execution
      boolean result =
          correctPromptCountsExternalValidator.isValid(quizResult, constraintValidatorContext);

      // Validation
      assertTrue(result);
      verifyNoInteractions(constraintValidatorContext);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 5, 9})
    void whenCorrectAnswersGreaterThanPromptCount_thenNotValid(int promptCount) {
      // Setup
      int correctAnswers = 10;
      quizResult =
          quizResultBuilder.correctAnswers(correctAnswers).promptCount(promptCount).build();

      // Execution
      boolean result =
          correctPromptCountsExternalValidator.isValid(quizResult, constraintValidatorContext);

      // Validation
      assertFalse(result);
      verifyNoInteractions(constraintValidatorContext);
    }
  }
}
