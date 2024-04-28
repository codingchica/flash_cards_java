package com.codingchica.flashcards.core.validators.internal;

import com.codingchica.flashcards.core.model.internal.QuizResult;
import com.codingchica.flashcards.core.validations.CorrectPromptCountsValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * A validator to confirm that minimumPrompts is less than or equal to maximumPrompts for a
 * FlashCardGroup.
 */
public class CorrectPromptCountsInternalValidator
    implements ConstraintValidator<CorrectPromptCountsValid, QuizResult> {

  @Override
  public boolean isValid(
      QuizResult quizResult, ConstraintValidatorContext constraintValidatorContext) {
    boolean valid = true;
    // Ignore null values for this validation. Those can be handled separately.
    if (quizResult != null) {
      if (quizResult.getCorrectAnswers() > quizResult.getPromptCount()) {
        valid = false;
      }
    }
    return valid;
  }
}
