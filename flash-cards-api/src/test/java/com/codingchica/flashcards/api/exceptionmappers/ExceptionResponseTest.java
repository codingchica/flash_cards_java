package com.codingchica.flashcards.api.exceptionmappers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ExceptionResponseTest {
  private ExceptionResponse.Builder exceptionResponseBuilder = ExceptionResponse.builder();

  private ExceptionResponse exceptionResponse = exceptionResponseBuilder.build();

  @Nested
  class BuilderTest {

    @Nested
    class CodeTest {
      @ParameterizedTest
      @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, 400, 500, Integer.MAX_VALUE})
      void whenSetViaBuilder_thenRetrievedInGetter(int code) {
        // Setup
        exceptionResponse = exceptionResponseBuilder.code(code).build();

        // Execution
        int result = exceptionResponse.getCode();

        // Validation
        assertEquals(code, result);
      }
    }

    @Nested
    class MessageTest {
      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {" ", "some value goes here"})
      void whenSetViaBuilder_thenRetrievedInGetter(String message) {
        // Setup
        exceptionResponse = exceptionResponseBuilder.message(message).build();

        // Execution
        String result = exceptionResponse.getMessage();

        // Validation
        assertEquals(message, result);
      }
    }

    @Nested
    class ToStringTest {

      @Test
      void whenInvoked_thenReturnsExpectedValue() {
        // Execution
        String result = exceptionResponseBuilder.toString();

        // Validation
        assertEquals("ExceptionResponse.Builder(code=0, message=null)", result);
      }
    }
  }
}
