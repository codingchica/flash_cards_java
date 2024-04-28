package com.codingchica.flashcards.core.model.external;

import com.codingchica.flashcards.core.validations.CorrectPromptCountsValid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/** A class to represent the result of a graded quiz externally. */
@Getter
@CorrectPromptCountsValid
@Builder(builderClassName = "Builder")
@ToString
public class QuizResult {
  /** The name of the quiz. */
  @NotBlank private String name;

  /** The number of questions that were present on the quiz. */
  @Min(0)
  private int promptCount = 0;

  /** The number of questions that were answered correctly on the quiz. */
  @Min(0)
  private int correctAnswers = 0;

  /** The percentage of questions that were answered correctly. */
  @Min(0)
  @Max(100)
  private int percentage = 0;

  /**
   * The duration in whole minutes.
   *
   * @see #getTimeSeconds() for partial minute details.
   */
  @Min(0)
  private int timeMinutes = 0;

  /**
   * The duration of partial minutes in whole seconds.
   *
   * @see #getTimeMinutes() for whole minute details.
   */
  @Min(0)
  private int timeSeconds = 0;
}
