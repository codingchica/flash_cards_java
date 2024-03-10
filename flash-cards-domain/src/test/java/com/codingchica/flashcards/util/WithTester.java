package com.codingchica.flashcards.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.NonNull;

public class WithTester {

  public static <C, T> void verifyWithValueCopyBehavior(
      @NonNull C objectUnderTest, T existingValue, T newValue, @NonNull Method withMethod)
      throws InvocationTargetException, IllegalAccessException {
    // Setup
    assertNotEquals(existingValue, newValue);

    // Execution
    Object resultSame = withMethod.invoke(objectUnderTest, existingValue);
    Object resultNew = withMethod.invoke(objectUnderTest, newValue);

    // Validation
    assertSame(
        objectUnderTest,
        resultSame,
        String.format(
            "Expected %s(%s) to reuse the same object for: %s",
            withMethod.getName(), existingValue, objectUnderTest));
    assertNotSame(
        objectUnderTest,
        resultNew,
        String.format(
            "Expected %s(%s) to create a new object for: %s",
            withMethod.getName(), newValue, objectUnderTest));
  }
}
