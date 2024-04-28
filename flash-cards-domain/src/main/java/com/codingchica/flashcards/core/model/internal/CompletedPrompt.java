package com.codingchica.flashcards.core.model.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/** A representation of the completed quiz's prompt, suitable for internal storage. */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CompletedPrompt {

  /** The prompt presented in the quiz. */
  @NotBlank private String prompt = null;

  /** The answer that was expected. */
  @NotBlank private String expectedAnswer = null;

  /** The answer that was provided. */
  @NotNull private String answerProvided = null;

  /** Whether the expected answer matched the provided answer. */
  private boolean correctAnswer = false;
}
