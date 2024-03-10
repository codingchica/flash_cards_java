package com.codingchica.flashcards.core.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RenderableExceptionTest {
  private String defaultMessage = "Error processing the request";

  @Nested
  class NoArgConstructorTest {
    @Test
    void whenInvoked_thenExpectedObjectReturned() {
      // Setup

      // Execution
      RenderableException renderableException = new RenderableException();

      // Validation
      assertNotNull(renderableException);
      assertAll(
          () ->
              assertEquals(
                  HttpStatus.INTERNAL_SERVER_ERROR_500,
                  renderableException.getHttpStatus(),
                  "httpStatus"),
          () -> assertEquals(defaultMessage, renderableException.getMessage(), "message"),
          () -> assertNull(renderableException.getCause()));
    }
  }

  @Nested
  class OneArgConstructorTest {
    @ParameterizedTest
    @ValueSource(
        ints = {
          HttpStatus.INTERNAL_SERVER_ERROR_500,
          HttpStatus.CONFLICT_409,
          HttpStatus.FORBIDDEN_403
        })
    void whenStatusCodeVaried_thenExpectedObjectReturned(int statusCode) {
      // Setup

      // Execution
      RenderableException renderableException = new RenderableException(statusCode);

      // Validation
      assertNotNull(renderableException);
      assertAll(
          () -> assertEquals(statusCode, renderableException.getHttpStatus(), "httpStatus"),
          () -> assertEquals(defaultMessage, renderableException.getMessage(), "message"),
          () -> assertNull(renderableException.getCause()));
    }
  }

  @Nested
  class TwoArgConstructorTest {
    @ParameterizedTest
    @ValueSource(
        ints = {
          HttpStatus.INTERNAL_SERVER_ERROR_500,
          HttpStatus.CONFLICT_409,
          HttpStatus.FORBIDDEN_403
        })
    void whenStatusCodeVaried_thenExpectedObjectReturned(int statusCode) {
      // Setup

      // Execution
      RenderableException renderableException = new RenderableException(statusCode, defaultMessage);

      // Validation
      assertNotNull(renderableException);
      assertAll(
          () -> assertEquals(statusCode, renderableException.getHttpStatus(), "httpStatus"),
          () -> assertEquals(defaultMessage, renderableException.getMessage(), "message"),
          () -> assertNull(renderableException.getCause()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "My Customized Error Message Goes Here"})
    void whenStatusCodeVaried_thenExpectedObjectReturned(String message) {
      // Setup

      // Execution
      RenderableException renderableException =
          new RenderableException(HttpStatus.FORBIDDEN_403, message);

      // Validation
      assertNotNull(renderableException);
      assertAll(
          () ->
              assertEquals(
                  HttpStatus.FORBIDDEN_403, renderableException.getHttpStatus(), "httpStatus"),
          () -> assertEquals(message, renderableException.getMessage(), "message"),
          () -> assertNull(renderableException.getCause()));
    }
  }

  @Nested
  class ThreeArgConstructorTest {
    @ParameterizedTest
    @ValueSource(
        ints = {
          HttpStatus.INTERNAL_SERVER_ERROR_500,
          HttpStatus.CONFLICT_409,
          HttpStatus.FORBIDDEN_403
        })
    void whenStatusCodeVaried_thenExpectedObjectReturned(int statusCode) {
      // Setup

      // Execution
      RenderableException renderableException =
          new RenderableException(statusCode, defaultMessage, null);

      // Validation
      assertNotNull(renderableException);
      assertAll(
          () -> assertEquals(statusCode, renderableException.getHttpStatus(), "httpStatus"),
          () -> assertEquals(defaultMessage, renderableException.getMessage(), "message"),
          () -> assertNull(renderableException.getCause()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "My Customized Error Message Goes Here"})
    void whenStatusCodeVaried_thenExpectedObjectReturned(String message) {
      // Setup

      // Execution
      RenderableException renderableException =
          new RenderableException(HttpStatus.FORBIDDEN_403, message, null);

      // Validation
      assertNotNull(renderableException);
      assertAll(
          () ->
              assertEquals(
                  HttpStatus.FORBIDDEN_403, renderableException.getHttpStatus(), "httpStatus"),
          () -> assertEquals(message, renderableException.getMessage(), "message"),
          () -> assertNull(renderableException.getCause()));
    }

    @Test
    void whenCausePopulated_thenExpectedObjectReturned() {
      // Setup
      Exception cause = new IllegalStateException("Something went wrong");

      // Execution
      RenderableException renderableException =
          new RenderableException(HttpStatus.FORBIDDEN_403, defaultMessage, cause);

      // Validation
      assertNotNull(renderableException);
      assertAll(
          () ->
              assertEquals(
                  HttpStatus.FORBIDDEN_403, renderableException.getHttpStatus(), "httpStatus"),
          () -> assertEquals(defaultMessage, renderableException.getMessage(), "message"),
          () -> assertSame(cause, renderableException.getCause()));
    }
  }
}
