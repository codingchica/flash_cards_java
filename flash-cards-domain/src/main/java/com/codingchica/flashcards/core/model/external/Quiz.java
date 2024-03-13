package com.codingchica.flashcards.core.model.external;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/** An external representation of a quiz. */
@Builder(builderClassName = "Builder")
@Getter
public class Quiz {
  /** A unique identifier for the quiz. * */
  private UUID id;

  /** The name of the quiz. */
  private String name;

  /** The time at which the quiz should be turned in - in order to pass. */
  @NotNull private String dueDateTime;

  /**
   * The prompts to present in the quiz, which may contain duplicates, depending upon the quiz
   * configuration.
   */
  private List<Map.Entry<String, String>> prompts;
}
