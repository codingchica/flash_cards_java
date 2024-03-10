package com.codingchica.flashcards.core.exceptions;

import lombok.Getter;
import org.eclipse.jetty.http.HttpStatus;

/**
 * An HTTP status code aware exception class to make it easier to map to the desired response code.
 */
public class RenderableException extends Throwable {
  /** The HTTP status code to use in the response. */
  @Getter private int httpStatus;

  /**
   * Constructor with a default code and error message.
   *
   * @see HttpStatus#INTERNAL_SERVER_ERROR_500
   */
  public RenderableException() {
    this(HttpStatus.INTERNAL_SERVER_ERROR_500);
  }

  /**
   * Constructor using a default error message.
   *
   * @param httpStatusCode The HTTP status code to use in the response.
   */
  public RenderableException(int httpStatusCode) {
    this(httpStatusCode, "Error processing the request");
  }

  /**
   * Constructor with no causedBy clause.
   *
   * @param httpStatusCode The HTTP status code to use in the response.
   * @param message The message to use in the response.
   */
  public RenderableException(int httpStatusCode, String message) {
    this(httpStatusCode, message, null);
  }

  /**
   * Constructor with a causedBy clause.
   *
   * @param httpStatusCode The HTTP status code to use in the response.
   * @param message The message to use in the response.
   * @param throwable The exception that was originally thrown, if any.
   */
  public RenderableException(int httpStatusCode, String message, Throwable throwable) {
    super(message, throwable);
    this.httpStatus = httpStatusCode;
  }
}
