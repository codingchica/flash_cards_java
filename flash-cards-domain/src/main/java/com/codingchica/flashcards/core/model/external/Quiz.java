package com.codingchica.flashcards.core.model.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.*;

/** An external representation of a quiz. */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(value = {"createdDateTime"})
public class Quiz {
  /** A unique identifier for the quiz. * */
  @NotNull private UUID id;

  /** The name of the quiz. */
  @NotBlank private String name;

  /** The time at which the quiz was created, for duration calculation purposes. */
  @PastOrPresent @NotNull private Instant createdDateTime;

  /**
   * The prompts to present in the quiz, which may contain duplicates, depending upon the quiz
   * configuration.
   */
  @NotEmpty private List<Map.Entry<String, String>> prompts;
}
