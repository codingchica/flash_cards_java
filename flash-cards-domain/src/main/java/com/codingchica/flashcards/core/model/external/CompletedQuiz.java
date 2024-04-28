package com.codingchica.flashcards.core.model.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.*;

/** An external representation of a completed quiz. */
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CompletedQuiz {

  @JsonProperty("name")
  @NotBlank
  private String name;

  @JsonProperty("answers")
  @NotEmpty
  private List<@NotBlank String> answers;

  @JsonProperty("inlineGrading")
  private boolean inlineGrading = false;
}
