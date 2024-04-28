package com.codingchica.flashcards.service;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.codingchica.flashcards.core.exceptions.RenderableException;
import com.codingchica.flashcards.core.mappers.external.CompletedQuizMapper;
import com.codingchica.flashcards.core.mappers.external.QuizMapper;
import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.core.model.external.QuizResult;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

/** The internal service which is responsible for quiz related logic. */
@Builder(builderClassName = "Builder")
public class QuizService {
  /** The application configuration for this instance. */
  @NonNull private FlashCardsConfiguration flashCardsConfiguration;

  @NonNull private QuizMapper quizMapper;

  @NonNull private CompletedQuizMapper completedQuizMapper;

  @Getter(AccessLevel.PACKAGE)
  private final Cache<UUID, Quiz> quizCache =
      CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(Duration.ofHours(3)).build();

  private final Map<String, List<FlashCardGroup>> flashCardGroupEntries = new HashMap<>();

  @NonNull private ObjectMapper objectMapper = null;

  /**
   * Lazy getter for the configured quizzes.
   *
   * @return Quizzes available.
   */
  @Synchronized
  private Map<String, List<FlashCardGroup>> getFlashCardConfigurations() {
    if (flashCardGroupEntries.isEmpty()) {
      Preconditions.checkNotNull(
          flashCardsConfiguration, "flashCardsConfiguration must not be null");
      Preconditions.checkNotNull(
          flashCardsConfiguration.getFlashCardGroupMap(), "flashCardGroupMap must not be null");

      flashCardGroupEntries.putAll(flashCardsConfiguration.getFlashCardGroupMap());
    }
    return Collections.unmodifiableMap(flashCardGroupEntries);
  }

  /**
   * Retrieve a particular quiz by name.
   *
   * @param quizName The name of the quiz to retrieve.
   * @return The corresponding Quiz, if found.
   */
  public Optional<Quiz> getQuiz(@NonNull String quizName) {
    Map<String, List<FlashCardGroup>> flashCardGroups = getFlashCardConfigurations();
    Preconditions.checkNotNull(flashCardGroups, "flashCardGroups must not be null");

    Optional<Quiz> optionalQuiz =
        flashCardGroups.entrySet().stream()
            .filter(Objects::nonNull)
            .filter(quizEntry -> quizEntry.getValue() != null)
            .flatMap(quizEntry -> quizEntry.getValue().stream())
            .filter(Objects::nonNull)
            .filter(quiz -> quiz.getName() != null)
            .filter(quiz -> StringUtils.equalsIgnoreCase(quizName, quiz.getName()))
            .map(quiz -> quizMapper.internalToExternalQuizMapping(quiz))
            .filter(Objects::nonNull)
            .findFirst();
    optionalQuiz.ifPresent(quiz -> quizCache.put(quiz.getId(), quiz));
    return optionalQuiz;
  }

  /**
   * Retrieve all available quiz names.
   *
   * @return A collection of available quiz names.
   */
  public Map<String, List<String>> listQuizNamesByCategory() {

    Map<String, List<FlashCardGroup>> flashCardGroups = getFlashCardConfigurations();
    Preconditions.checkNotNull(flashCardGroups, "flashCardGroups must not be null");

    return flashCardGroups.entrySet().stream()
        .filter(Objects::nonNull)
        .filter(entry -> entry.getKey() != null)
        .filter(entry -> entry.getValue() != null)
        .filter(entry -> entry.getValue().size() > 0)
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(FlashCardGroup::getName).toList()));
  }

  /**
   * Submit a completed quiz for grading.
   *
   * @param id The ID of the quiz.
   * @param completedQuiz The quiz to grade.
   * @return A graded quiz result.
   * @throws RenderableException when the requested quiz is not found in the server.
   */
  public QuizResult gradeQuiz(@NonNull UUID id, @NonNull CompletedQuiz completedQuiz)
      throws RenderableException {
    Quiz quiz = quizCache.getIfPresent(id);
    if (quiz == null) {
      throw new RenderableException(
          HttpStatus.NOT_FOUND_404, String.format("Quiz='%s' not found", id));
    }
    if (!StringUtils.equals(quiz.getName(), completedQuiz.getName())) {
      throw new RenderableException(HttpStatus.NOT_FOUND_404, "Quiz name mismatch");
    }
    QuizResult externalQuizResult =
        completedQuizMapper.mapCompletedQuizToExternalResults(quiz, completedQuiz);
    saveQuizResult(externalQuizResult);
    return externalQuizResult;
  }

  /**
   * Store the graded quiz to the file system.
   *
   * @param quizResult The quiz results which should be stored to the file system.
   * @throws RenderableException Thrown if there is an issue while saving the quiz results.
   */
  protected void saveQuizResult(@NonNull QuizResult quizResult) throws RenderableException {

    try {
      objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
      objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
      objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("temp.json"), quizResult);
    } catch (IOException e) {
      throw new RenderableException(
          HttpStatus.INTERNAL_SERVER_ERROR_500, "Error while saving quiz results.");
    }
  }
}
