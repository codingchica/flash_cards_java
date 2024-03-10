package com.codingchica.flashcards.core.validations;

import com.codingchica.flashcards.core.validators.MinMaxPromptCountsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** An annotation to enforce that minimumPrompts is less than or equal to maximumPrompts. */
@Constraint(validatedBy = {MinMaxPromptCountsValidator.class})
@Target({
  ElementType.TYPE,
})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MinMaxPromptCountsValid {

  /**
   * The default message to return, if none is provided in the annotation.
   *
   * @return Either the message provided by the calling code or a default.
   */
  String message() default "minimumPrompts must not be larger than maximumPrompts";

  /**
   * Allow the calling code to optionally group validations into types/levels.
   *
   * @return By default, the validation will be applied to all groups.
   */
  Class<?>[] groups() default {};

  /**
   * Payload(s) to invoke if the validation fails.
   *
   * @return An array of payloads to invoke upon constraint violations.
   */
  Class<? extends Payload>[] payload() default {};
}
