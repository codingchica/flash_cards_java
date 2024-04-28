package com.codingchica.flashcards.core.model.internal;

import com.codingchica.flashcards.core.validations.CorrectPromptCountsValid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/** A class to represent the result of a graded quiz internally. */
@Getter
@CorrectPromptCountsValid
@Builder(builderClassName = "Builder")
@ToString
public class QuizResult {

  /** A unique identifier for the quiz. * */
  @NotNull private UUID id;

  /** The name of the quiz. */
  @NotBlank private String name;

  /** The time at which the quiz was created, for duration calculation purposes. */
  @PastOrPresent @NotNull private Instant createdDateTime;

  /** The number of questions that were present on the quiz. */
  @Min(0)
  private int promptCount;

  /** The number of questions that were answered correctly on the quiz. */
  @Min(0)
  private int correctAnswers;

  /** The percentage of questions that were answered correctly. */
  @Min(0)
  @Max(100)
  private int percentage;

  /**
   * The duration in whole minutes.
   *
   * @see #getTimeSeconds() for partial minute details.
   */
  @Min(0)
  private int timeMinutes;

  /**
   * The duration of partial minutes in whole seconds.
   *
   * @see #getTimeMinutes() for whole minute details.
   */
  @Min(0)
  private int timeSeconds;

  private List<CompletedPrompt> completedPrompts;
}
