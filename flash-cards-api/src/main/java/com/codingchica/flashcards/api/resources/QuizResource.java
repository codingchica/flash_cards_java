package com.codingchica.flashcards.api.resources;

import com.codingchica.flashcards.core.exceptions.RenderableException;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.service.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;

/** The Web service entry point into the application for CRUD operations involving quizzes. */
@Path("/quizzes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Builder(builderClassName = "Builder")
public class QuizResource {
  @Getter(AccessLevel.PROTECTED)
  @NonNull private QuizService quizService;

  /**
   * Retrieve the quiz names available.
   *
   * @return A collection of quiz names available.
   */
  @GET
  public List<String> listQuizzes() {
    List<Quiz> quizzes = quizService.listQuizzes();
    List<String> quizNames = Collections.EMPTY_LIST;
    if (quizzes != null) {
      quizNames =
          quizzes.stream()
              .filter(Objects::nonNull)
              .filter(quiz -> StringUtils.isNotBlank(quiz.getName()))
              .map(Quiz::getName)
              .collect(Collectors.toList());
    }
    return quizNames;
  }

  /**
   * Retrieve a particular quiz by name.
   *
   * @param quizName The name of the quiz to retrieve.
   * @return The corresponding quiz, if available.
   * @throws RenderableException if no matching quiz is found.
   */
  @GET
  @Path("/{quizName}")
  public @Valid Quiz getQuiz(@PathParam("quizName") @NotBlank String quizName)
      throws RenderableException {
    return quizService
        .getQuiz(quizName)
        .orElseThrow(
            () ->
                new RenderableException(
                    HttpStatus.NOT_FOUND_404,
                    String.format("No match found for quiz: '%s'", quizName)));
  }
}
