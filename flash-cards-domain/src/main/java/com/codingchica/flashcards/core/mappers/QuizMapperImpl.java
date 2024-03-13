package com.codingchica.flashcards.core.mappers;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public class QuizMapperImpl implements QuizMapper {

  @Getter(AccessLevel.PACKAGE)
  @lombok.Builder.Default
  @NonNull private ObjectIdGenerators.UUIDGenerator uuidGenerator = new ObjectIdGenerators.UUIDGenerator();

  @Getter(AccessLevel.PACKAGE)
  @lombok.Builder.Default
  @NonNull private Random random = new Random();

  @Override
  public Quiz internalToExternalQuizMapping(
      @NonNull String name, @NonNull FlashCardGroup flashCardGroup) {
    Quiz.Builder quizBuilder =
        Quiz.builder()
            .id(uuidGenerator.generateId(flashCardGroup))
            .name(name)
            .prompts(
                getPrompts(
                    flashCardGroup.getMinimumPrompts(),
                    flashCardGroup.getMaximumPrompts(),
                    flashCardGroup.getPrompts()));

    Duration maxDuration = flashCardGroup.getMaxDuration();
    if (maxDuration != null) {
      Instant dueDateTime = Instant.now().plus(maxDuration);
      quizBuilder.dueDateTime(dueDateTime.toString());
    }

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
