package com.codingchica.flashcards.core.mappers.external;

import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.core.model.external.QuizResult;
import com.codingchica.flashcards.core.model.internal.CompletedPrompt;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

/**
 * A mapper to translate already completed quiz information into internal and external
 * representations.
 */
public class CompletedQuizMapperImpl implements CompletedQuizMapper {

  /**
   * Construct an external QuizResult from quiz and completed quiz information.
   *
   * @param quiz The quiz which was presented to the user.
   * @param completedQuiz The completed quiz that was returned to the server.
   * @return An external representation of a quiz result.
   */
  public QuizResult mapCompletedQuizToExternalResults(
      @NonNull Quiz quiz, @NonNull CompletedQuiz completedQuiz) {
    Preconditions.checkNotNull(quiz.getPrompts(), "quiz.getPrompts() must not be null");
    Preconditions.checkNotNull(
        quiz.getCreatedDateTime(), "quiz.getCreatedDateTime() must not be null");
    int promptCount = (int) quiz.getPrompts().stream().filter(Objects::nonNull).count();
    int correctCount = getCorrectAnswerCount(quiz, completedQuiz);
    int percentage = getCorrectPercentage(correctCount, promptCount);
    Duration duration = Duration.between(quiz.getCreatedDateTime(), Instant.now());
    QuizResult quizResult =
        QuizResult.builder()
            .promptCount(promptCount)
            .correctAnswers(correctCount)
            .name(quiz.getName())
            .percentage(percentage)
            .timeMinutes(duration.toMinutesPart())
            .timeSeconds(duration.toSecondsPart())
            .build();
    return quizResult;
  }

  /**
   * Construct an internal QuizResult from quiz and completed quiz information.
   *
   * @param quiz The quiz which was presented to the user.
   * @param completedQuiz The completed quiz that was returned to the server.
   * @return An internal representation of a quiz result.
   */
  @Override
  public com.codingchica.flashcards.core.model.internal.QuizResult
      mapCompletedQuizToInternalResults(@NonNull Quiz quiz, @NonNull CompletedQuiz completedQuiz) {
    Preconditions.checkNotNull(quiz.getPrompts(), "quiz.getPrompts() must not be null");
    Preconditions.checkNotNull(
        quiz.getCreatedDateTime(), "quiz.getCreatedDateTime() must not be null");
    int promptCount = (int) quiz.getPrompts().stream().filter(Objects::nonNull).count();
    int correctCount = getCorrectAnswerCount(quiz, completedQuiz);
    int percentage = getCorrectPercentage(correctCount, promptCount);
    Duration duration = Duration.between(quiz.getCreatedDateTime(), Instant.now());
    com.codingchica.flashcards.core.model.internal.QuizResult quizResult =
        com.codingchica.flashcards.core.model.internal.QuizResult.builder()
            .id(quiz.getId())
            .name(quiz.getName())
            .createdDateTime(quiz.getCreatedDateTime())
            .promptCount(promptCount)
            .correctAnswers(correctCount)
            .percentage(percentage)
            .timeMinutes(duration.toMinutesPart())
            .timeSeconds(duration.toSecondsPart())
            .completedPrompts(getCompletedPrompts(quiz.getPrompts(), completedQuiz.getAnswers()))
            .build();
    return quizResult;
  }

  private List<CompletedPrompt> getCompletedPrompts(
      List<Map.Entry<String, String>> prompts, List<String> answers) {
    Preconditions.checkNotNull(prompts, "prompts must not be null");
    Preconditions.checkNotNull(answers, "answers must not be null");
    List<CompletedPrompt> completedPrompts = new ArrayList<>();
    Preconditions.checkArgument(
        prompts.size() == answers.size(),
        String.format(
            "prompts (%s) and answers (%s) size must be equivalent",
            prompts.size(), answers.size()));
    for (int i = 0; i < prompts.size(); i++) {
      Map.Entry<String, String> prompt = prompts.get(i);
      String answer = answers.get(i);
      if (prompt == null) {
        continue;
      }
      CompletedPrompt completedPrompt =
          CompletedPrompt.builder()
              .prompt(prompt.getKey())
              .expectedAnswer(prompt.getValue())
              .answerProvided(answer)
              .correctAnswer(StringUtils.equalsIgnoreCase(answer, prompt.getValue()))
              .build();
      completedPrompts.add(completedPrompt);
    }
    return completedPrompts;
  }

  private int getCorrectPercentage(int correctCount, int promptCount) {
    int percentage = 0;
    if (promptCount > 0) {
      percentage = correctCount * 100 / promptCount;
    }
    return percentage;
  }

  private int getCorrectAnswerCount(Quiz quiz, CompletedQuiz completedQuiz) {
    int correctCount = 0;
    Preconditions.checkNotNull(quiz, "quiz must not be null");
    Preconditions.checkNotNull(completedQuiz, "completedQuiz must not be null");
    List<String> answers = completedQuiz.getAnswers();
    List<Map.Entry<String, String>> prompts = quiz.getPrompts();
    Preconditions.checkNotNull(answers, "answers must not be null");
    Preconditions.checkNotNull(prompts, "prompts must not be null");
    Preconditions.checkArgument(
        quiz.getPrompts().size() == completedQuiz.getAnswers().size(),
        String.format(
            "prompts (%s) and answers (%s) size must be equivalent",
            prompts.size(), answers.size()));
    for (int i = 0; i < quiz.getPrompts().size(); i++) {
      Map.Entry<String, String> prompt = quiz.getPrompts().get(i);
      if (prompt == null) {
        continue;
      }
      String expectedResult = prompt.getValue();
      String answer = completedQuiz.getAnswers().get(i);
      if (StringUtils.equals(expectedResult, answer)) {
        correctCount++;
      }
    }
    return correctCount;
  }
}
