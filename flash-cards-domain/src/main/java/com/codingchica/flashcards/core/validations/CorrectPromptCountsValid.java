package com.codingchica.flashcards.core.validations;

import com.codingchica.flashcards.core.validators.external.CorrectPromptCountsExternalValidator;
import com.codingchica.flashcards.core.validators.internal.CorrectPromptCountsInternalValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** An annotation to enforce that correctAnswers is less than or equal to promptCount. */
@Constraint(
    validatedBy = {
      CorrectPromptCountsExternalValidator.class,
      CorrectPromptCountsInternalValidator.class
    })
@Target({
  ElementType.TYPE,
})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CorrectPromptCountsValid {

  /**
   * The default message to return, if none is provided in the annotation.
   *
   * @return Either the message provided by the calling code or a default.
   */
  String message() default "correctAnswers must not be larger than promptCount";

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
