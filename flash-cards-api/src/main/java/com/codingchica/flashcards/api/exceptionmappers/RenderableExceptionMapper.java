package com.codingchica.flashcards.api.exceptionmappers;

import com.codingchica.flashcards.core.exceptions.RenderableException;
import com.google.common.base.Preconditions;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Class to map a RenderableException into a Response suitable for emitting from the API. This helps
 * to ensure that there is no information leakage (stack traces, etc.) in the response returned.
 */
public class RenderableExceptionMapper implements ExceptionMapper<RenderableException> {

  /**
   * Map a RenderableException to an API-appropriate Response.
   *
   * @param exception The exception to map into an API response.
   * @return The response to return for an API call.
   */
  public Response toResponse(RenderableException exception) {
    Preconditions.checkNotNull(exception, "exception must not be null");
    return Response.status(exception.getHttpStatus())
        .entity(getBody(exception))
        .type(MediaType.APPLICATION_JSON_TYPE)
        .build();
  }

  /**
   * Map the RenderableException into an ExceptionResponse to use in the Response body.
   *
   * @param exception The RenderableException to map into an ExceptionResponse.
   * @return The ExceptionResponse that is appropriate for use in a Response body.
   */
  public ExceptionResponse getBody(RenderableException exception) {
    Preconditions.checkNotNull(exception, "exception must not be null");
    return ExceptionResponse.builder()
        .code(exception.getHttpStatus())
        .message(exception.getMessage())
        .build();
  }
}
