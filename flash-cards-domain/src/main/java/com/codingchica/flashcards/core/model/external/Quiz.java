package com.codingchica.flashcards.core.model.external;

import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
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
  @NotNull private ZonedDateTime dueDateTime;

  /** The prompts to present in the quiz. */
  private Map<String, String> prompts;
}
