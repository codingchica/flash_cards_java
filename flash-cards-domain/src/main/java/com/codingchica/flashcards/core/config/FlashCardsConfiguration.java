package com.codingchica.flashcards.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * The POJO representing the application configuration that will be used when running the server.
 * See appConfig/README.md.
 */
@ToString
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@NoArgsConstructor
@Setter // TODO would like to make this POJO not have setters, so it cannot be modified after
// creation.
@Getter
public class FlashCardsConfiguration extends Configuration {
  @JsonProperty("flashCardGroupMap")
  @NotEmpty
  private Map<
          @Length(max = 30, message = "must be 30 characters or less") @NotBlank
          @Pattern(
              regexp = "[\\w \\d]*",
              message = "must contain only alpha-numeric characters and spaces")
          String,
          @Valid @NotNull FlashCardGroup>
      flashCardGroupMap;
}
