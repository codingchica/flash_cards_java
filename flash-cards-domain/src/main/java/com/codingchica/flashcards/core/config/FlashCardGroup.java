package com.codingchica.flashcards.core.config;

import com.codingchica.flashcards.core.validations.MinMaxPromptCountsValid;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.util.Map;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;

/** The POJO representing a group of related flash cards to use in a given quiz. */
@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
@MinMaxPromptCountsValid
@Setter
@Builder(builderClassName = "Builder")
public class FlashCardGroup {
  /**
   * The maximum number of prompts to present to a user on a given execution. The value of 0 means
   * to use all prompts configured. Any positive value will enforce a maximum limit on the prompts
   * consumed.
   */
  @JsonProperty("maximumPrompts")
  @PositiveOrZero
  private int maximumPrompts;

  /**
   * The minimum number of prompts to present to a user on a given execution. Any positive value
   * will enforce a minimum limit on the prompts consumed, which may require repeat prompts, if the
   * number of prompts available is less than the minimum.
   */
  @JsonProperty("minimumPrompts")
  @PositiveOrZero
  private int minimumPrompts;

  /** The maximum amount of time that can be taken for the quiz to be considered successful. */
  @NotNull @DurationMax(hours = 1)
  @DurationMin(seconds = 10)
  private Duration maxDuration;

  /** The prompts -&gt; answers to use for the group. */
  @NotEmpty
  @JsonProperty("prompts")
  private Map<
          @NotBlank @Length(max = 50, message = "must be 50 characters or less") String,
          @NotBlank @Length(max = 50, message = "must be 50 characters or less") String>
      prompts;
}
