package com.codingchica.flashcards.api.resources;

import com.codingchica.flashcards.core.exceptions.RenderableException;
import com.codingchica.flashcards.core.model.external.CompletedQuiz;
import com.codingchica.flashcards.core.model.external.Quiz;
import com.codingchica.flashcards.core.model.external.QuizResult;
import com.codingchica.flashcards.service.QuizService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
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
  public Map<String, List<String>> listQuizzes() {
    return quizService.listQuizNamesByCategory();
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

  /**
   * Submit a completed quiz for grading.
   *
   * @param id The unique ID of the quiz.
   * @param completedQuiz The results from the quiz.
   * @return The result of the graded quiz.
   * @throws RenderableException when the requested quiz is not found in the server.
   */
  @POST
  @Path("/{quizName}/{id}")
  public @Valid QuizResult gradeQuiz(
      @PathParam(("id")) @NotNull UUID id, @NotNull CompletedQuiz completedQuiz)
      throws RenderableException {
    return quizService.gradeQuiz(id, completedQuiz);
  }
}
