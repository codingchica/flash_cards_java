package com.codingchica.flashcards.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reusable unit tests that validate getter/setter logic, since that is such boilerplate test code.
 */
public class GetterSetterTester {

  /**
   * Validate that a given value is set and retrieved successfully.
   *
   * @param objectUnderTest An instance of the object under test.
   * @param fieldValue The value to use in the getter/setter test.
   * @param getter The getter method to invoke. Should consume no arguments. Should return the field
   *     value.
   * @param setter The setter method to invoke. Should consume only one argument, the field value.
   * @param <C> The class under test.
   * @param <T> The field type for which the getter/setter is being tested.
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  public static <C, T> void valueRetrievedCorrectly(
      C objectUnderTest, T fieldValue, Method getter, Method setter)
      throws InvocationTargetException, IllegalAccessException {
    // Setup
    setter.invoke(objectUnderTest, fieldValue);

    // Execution
    Object result = getter.invoke(objectUnderTest);

    // Validation
    // Same works for objects, but doesn't work for primitives.
    assertEquals(
        fieldValue,
        result,
        getter.getName() + " didn't return the same object as used in " + setter.getName());
  }
}
