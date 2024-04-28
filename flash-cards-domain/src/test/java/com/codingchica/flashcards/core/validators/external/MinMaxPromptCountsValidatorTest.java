package com.codingchica.flashcards.core.validators.external;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MinMaxPromptCountsValidatorTest {
  private FlashCardGroup.Builder flashCardGroupBuilder = FlashCardGroup.builder();

  private FlashCardGroup flashCardGroup = flashCardGroupBuilder.build();

  private MinMaxPromptCountsValidator minMaxPromptCountsValidator =
      new MinMaxPromptCountsValidator();
  private ConstraintValidatorContext constraintValidatorContext =
      mock(ConstraintValidatorContext.class);

  @Nested
  class IsValidTest {
    @Test
    void whenFlashCardGroupNull_thenValid() {
      // Execution
      boolean result = minMaxPromptCountsValidator.isValid(null, constraintValidatorContext);

      // Validation
      assertTrue(result);
      verifyNoInteractions(constraintValidatorContext);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 5, 10})
    void whenMinimumPromptsLessThanOrEqualToMaximumPrompts_thenValid(int minimumPrompts) {
      // Setup
      int maximumPromptsValue = 10;
      flashCardGroup =
          flashCardGroupBuilder
              .minimumPrompts(minimumPrompts)
              .maximumPrompts(maximumPromptsValue)
              .build();

      // Execution
      boolean result =
          minMaxPromptCountsValidator.isValid(flashCardGroup, constraintValidatorContext);

      // Validation
      assertTrue(result);
      verifyNoInteractions(constraintValidatorContext);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 200, Integer.MAX_VALUE})
    void whenMaximumPromptsGreaterThanOrEqualToMinimumPrompts_thenValid(int maximumPromptsValue) {
      // Setup
      int minimumPrompts = 5;
      flashCardGroup =
          flashCardGroupBuilder
              .minimumPrompts(minimumPrompts)
              .maximumPrompts(maximumPromptsValue)
              .build();

      // Execution
      boolean result =
          minMaxPromptCountsValidator.isValid(flashCardGroup, constraintValidatorContext);

      // Validation
      assertTrue(result);
      verifyNoInteractions(constraintValidatorContext);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 5, 9})
    void whenMinimumPromptsGreaterThanMaximumPrompts_thenNotValid(int maximumPromptsValue) {
      // Setup
      int minimumPrompts = 10;
      flashCardGroup =
          flashCardGroupBuilder
              .minimumPrompts(minimumPrompts)
              .maximumPrompts(maximumPromptsValue)
              .build();

      // Execution
      boolean result =
          minMaxPromptCountsValidator.isValid(flashCardGroup, constraintValidatorContext);

      // Validation
      assertFalse(result);
      verifyNoInteractions(constraintValidatorContext);
    }
  }
}
