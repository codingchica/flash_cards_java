package com.codingchica.flashcards.core.validators.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.codingchica.flashcards.core.model.internal.QuizResult;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CorrectPromptCountsInternalValidatorTest {
  private QuizResult.Builder quizResultBuilder = QuizResult.builder();

  private QuizResult quizResult = quizResultBuilder.build();

  private CorrectPromptCountsInternalValidator CorrectPromptCountsInternalValidator =
      new CorrectPromptCountsInternalValidator();
  private ConstraintValidatorContext constraintValidatorContext =
      mock(ConstraintValidatorContext.class);

  @Nested
  class IsValidTest {
    @Test
    void whenFlashCardGroupNull_thenValid() {
      // Execution
      boolean result =
          CorrectPromptCountsInternalValidator.isValid(null, constraintValidatorContext);

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
          CorrectPromptCountsInternalValidator.isValid(quizResult, constraintValidatorContext);

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
          CorrectPromptCountsInternalValidator.isValid(quizResult, constraintValidatorContext);

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
          CorrectPromptCountsInternalValidator.isValid(quizResult, constraintValidatorContext);

      // Validation
      assertFalse(result);
      verifyNoInteractions(constraintValidatorContext);
    }
  }
}
