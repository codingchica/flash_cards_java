package com.codingchica.flashcards.core.validators.external;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.validations.MinMaxPromptCountsValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * A validator to confirm that minimumPrompts is less than or equal to maximumPrompts for a
 * FlashCardGroup.
 */
public class MinMaxPromptCountsValidator
    implements ConstraintValidator<MinMaxPromptCountsValid, FlashCardGroup> {

  @Override
  public boolean isValid(
      FlashCardGroup flashCardGroup, ConstraintValidatorContext constraintValidatorContext) {
    boolean valid = true;
    // Ignore null values for this validation. Those can be handled separately.
    if (flashCardGroup != null) {
      if (flashCardGroup.getMinimumPrompts() > flashCardGroup.getMaximumPrompts()) {
        valid = false;
      }
    }
    return valid;
  }
}
