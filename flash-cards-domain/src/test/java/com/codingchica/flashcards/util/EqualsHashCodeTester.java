package com.codingchica.flashcards.util;

import static org.junit.jupiter.api.Assertions.*;

import lombok.NonNull;

public class EqualsHashCodeTester {

  public static <C> void verifyEqualsCanEqualAndHashCodeForMatch(
      @NonNull C object1, @NonNull C object2) {
    assertEquals(object1, object2, "equals");
    assertEquals(object1.hashCode(), object2.hashCode(), "hashcode");
  }

  public static <C> void verifyEqualsCanEqualAndHashCodeForNotMatch(
      @NonNull C object1, @NonNull C object2) {
    assertNotEquals(object1, object2, "equals");
    // While this isn't guaranteed for all combinations, it should be stable, as long as the values
    // aren't generated by the test at runtime.
    assertNotEquals(object1.hashCode(), object2.hashCode(), "hashcode");
  }

  public static <C> void verifyEqualsForOtherScenarios(@NonNull C object1) {
    assertFalse(object1.equals(null), "null");
    assertFalse(object1.equals("Hello world"), "Another class");
  }
}
