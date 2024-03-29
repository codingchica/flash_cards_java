package com.codingchica.flashcards.service;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.codingchica.flashcards.core.mappers.QuizMapper;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.google.common.base.Preconditions;
import java.util.*;
import lombok.Builder;
import lombok.NonNull;
import lombok.Synchronized;
import org.apache.commons.lang3.StringUtils;

/** The internal service which is responsible for quiz related logic. */
@Builder(builderClassName = "Builder")
public class QuizService {
  /** The application configuration for this instance. */
  @NonNull private FlashCardsConfiguration flashCardsConfiguration;

  @NonNull private QuizMapper quizMapper;

  private final List<Map.Entry<String, FlashCardGroup>> flashCardGroupEntries = new ArrayList<>();

  /**
   * Lazy getter for the configured quizzes.
   *
   * @return Quizzes available.
   */
  @Synchronized
  private List<Map.Entry<String, FlashCardGroup>> getFlashCardConfigurations() {
    if (flashCardGroupEntries.isEmpty()) {
      Preconditions.checkNotNull(
          flashCardsConfiguration, "flashCardsConfiguration must not be null");
      Preconditions.checkNotNull(
          flashCardsConfiguration.getFlashCardGroupMap(), "flashCardGroupMap must not be null");
      List<Map.Entry<String, FlashCardGroup>> flashCardGroupsFound =
          flashCardsConfiguration.getFlashCardGroupMap().entrySet().stream()
              .filter(Objects::nonNull)
              .filter(entry -> entry.getKey() != null)
              .filter(entry -> entry.getValue() != null)
              .toList();

      flashCardGroupEntries.addAll(flashCardGroupsFound);
    }
    return Collections.unmodifiableList(flashCardGroupEntries);
  }

  /**
   * Retrieve a particular quiz by name.
   *
   * @param quizName The name of the quiz to retrieve.
   * @return The corresponding Quiz, if found.
   */
  public Optional<Quiz> getQuiz(@NonNull String quizName) {
    List<Map.Entry<String, FlashCardGroup>> flashCardGroupEntities = getFlashCardConfigurations();
    Preconditions.checkNotNull(flashCardGroupEntities, "flashCardGroupEntities must not be null");

    return flashCardGroupEntities.stream()
        .filter(Objects::nonNull)
        .filter(quizEntry -> StringUtils.equalsIgnoreCase(quizName, quizEntry.getKey()))
        .map(
            quizEntry ->
                quizMapper.internalToExternalQuizMapping(quizEntry.getKey(), quizEntry.getValue()))
        .filter(Objects::nonNull)
        .findFirst();
  }

  /**
   * Retrieve all available quiz names.
   *
   * @return A collection of available quiz names.
   */
  public List<String> listQuizNames() {

    List<Map.Entry<String, FlashCardGroup>> flashCardGroupEntities = getFlashCardConfigurations();
    Preconditions.checkNotNull(flashCardGroupEntities, "flashCardGroupEntities must not be null");

    return flashCardGroupEntities.stream()
        .filter(Objects::nonNull)
        .map(flashCardGroupEntry -> flashCardGroupEntry.getKey())
        .toList();
  }
}
