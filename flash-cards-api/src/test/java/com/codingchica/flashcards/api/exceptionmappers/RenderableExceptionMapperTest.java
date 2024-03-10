package com.codingchica.flashcards.api.exceptionmappers;

import static org.junit.jupiter.api.Assertions.*;

import com.codingchica.flashcards.core.exceptions.RenderableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RenderableExceptionMapperTest {

  private RenderableExceptionMapper renderableExceptionMapper = new RenderableExceptionMapper();

  @Nested
  class ToResponseTest {
    @Test
    void whenExceptionNull_thenExceptionThrown() {
      // Execution
      Executable executable = () -> renderableExceptionMapper.toResponse(null);

      // Validation
      Exception exception = assertThrows(NullPointerException.class, executable);
      assertEquals("exception must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "Some exception message here"})
    void whenExceptionMessageVaried_thenSameUsedInResponseBody(String exceptionMessage) {
      // Setup
      int code = HttpStatus.NOT_FOUND_404;
      RenderableException exception = new RenderableException(code, exceptionMessage);

      // Execution
      Response response = renderableExceptionMapper.toResponse(exception);

      // Validation
      assertNotNull(response);
      assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

      assertTrue(response.getEntity() instanceof ExceptionResponse);
      ExceptionResponse exceptionResponse = (ExceptionResponse) response.getEntity();
      assertAll(
          () -> assertEquals(exceptionMessage, exceptionResponse.getMessage()),
          () -> assertEquals(code, exceptionResponse.getCode()),
          () -> assertEquals(code, response.getStatus()));
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 404, 500, Integer.MAX_VALUE})
    void whenExceptionMessageVaried_thenSameUsedInResponseBody(int code) {
      // Setup
      String exceptionMessage = "some message goes here";
      RenderableException exception = new RenderableException(code, exceptionMessage);

      // Execution
      Response response = renderableExceptionMapper.toResponse(exception);

      // Validation
      assertNotNull(response);
      assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

      assertTrue(response.getEntity() instanceof ExceptionResponse);
      ExceptionResponse exceptionResponse = (ExceptionResponse) response.getEntity();
      assertAll(
          () -> assertEquals(exceptionMessage, exceptionResponse.getMessage()),
          () -> assertEquals(code, exceptionResponse.getCode()),
          () -> assertEquals(code, response.getStatus()));
    }
  }
}
