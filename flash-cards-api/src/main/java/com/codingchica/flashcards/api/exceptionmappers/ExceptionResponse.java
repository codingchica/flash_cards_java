package com.codingchica.flashcards.api.exceptionmappers;

import lombok.Builder;
import lombok.Getter;

/** An Object appropriate for use in a Response body when an exception is thrown. */
@Builder(builderClassName = "Builder")
@Getter
public class ExceptionResponse {

  /**
   * The HTTP status code for the error. See also DropWizard error response for 404, etc., which
   * also use the code field.
   */
  private int code;

  /**
   * A human-readable message indicating the error encountered. See also DropWizard error response
   * for 404, etc., which also use the message field.
   */
  private String message;
}
