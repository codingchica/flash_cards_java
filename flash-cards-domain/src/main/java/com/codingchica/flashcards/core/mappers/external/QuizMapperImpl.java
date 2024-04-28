package com.codingchica.flashcards.core.mappers.external;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.base.Preconditions;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/** Mapper for constructing external QuizMapper objects. */
@Builder(builderClassName = "Builder")
public class QuizMapperImpl implements QuizMapper {

  @Getter(AccessLevel.PACKAGE)
  @lombok.Builder.Default
  @NotNull private ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();

  @Getter(AccessLevel.PACKAGE)
  @lombok.Builder.Default
  @NotNull private Random random = new Random();

  /**
   * Map a FlashCardGroup to a Quiz for external rendering/representation.
   *
   * @param flashCardGroup The flashCardGroup from the configuration.
   * @return The external representation of the quiz.
   */
  @Override
  public Quiz internalToExternalQuizMapping(@NonNull FlashCardGroup flashCardGroup) {
    Quiz.Builder quizBuilder =
        Quiz.builder()
            .id(uuidGenerator.generateId(flashCardGroup))
            .name(flashCardGroup.getName())
            .createdDateTime(Instant.now())
            .prompts(
                getPrompts(
                    flashCardGroup.getMinimumPrompts(),
                    flashCardGroup.getMaximumPrompts(),
                    flashCardGroup.getPrompts()));

    return quizBuilder.build();
  }

  private List<Map.Entry<String, String>> getPrompts(
      int minPrompts, int maxPrompts, @NonNull Map<String, String> promptsMap) {
    int max = Math.max(promptsMap.size(), minPrompts);
    if (maxPrompts > 0) {
      max = Math.min(max, maxPrompts);
    }
    int promptCopies = getCopiesCount(minPrompts, promptsMap.size());
    List<Map.Entry<String, String>> prompts =
        promptsMap.entrySet().parallelStream()
            .flatMap(
                entry -> {
                  // Duplicate the current entry enough to make sure we can meet any minimum
                  // expectations
                  List<Map.Entry<String, String>> copies = new ArrayList<>();
                  for (int i = 0; i < promptCopies; i++) {
                    copies.add(entry);
                  }
                  return copies.stream();
                })
            .collect(Collectors.toList());
    Collections.shuffle(prompts);
    return prompts.stream()
        // Limit to the maximum we expect, if any.
        .limit(max)
        .toList();
  }

  /**
   * How many copies of the prompts should be generated in order to fill out the minimum prompt
   * count requested.
   *
   * @param minPrompts How many prompts are desired, at minimum.
   * @param promptsMapSize The count of the configured prompts.
   * @return The number of times the prompts collection should be copied in order to assure that the
   *     minimum is met.
   */
  protected static int getCopiesCount(int minPrompts, int promptsMapSize) {
    Preconditions.checkArgument(promptsMapSize > 0, "promptsMapSize must be greater than 0");
    int max = promptsMapSize;
    int min = Math.max(1, minPrompts);

    int fullCopies = (int) Math.ceil(min / promptsMapSize);
    int remainder = min % max;
    if (remainder > 0) {
      fullCopies++;
    }
    return fullCopies;
  }
}
