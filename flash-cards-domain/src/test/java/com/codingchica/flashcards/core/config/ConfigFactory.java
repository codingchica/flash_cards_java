package com.codingchica.flashcards.core.config;

import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

/** A factory for generating happy-path configurations to use in testing. */
public class ConfigFactory {

  /**
   * Construct and populate a valid FlashCardsConfiguration.Builder that can be used for happy-path
   * testing.
   *
   * @return A populated FlashCardsConfiguration.Builder object, setup for validation happy-path.
   */
  public static FlashCardsConfiguration.Builder flashCardsConfigurationBuilder() {
    Map<String, FlashCardGroup> groups = new TreeMap<>();
    groups.put("flashcardGroup", flashCardGroup());
    return FlashCardsConfiguration.builder().flashCardGroupMap(groups);
  }

  /**
   * Construct and populate a valid FlashCardsConfiguration that can be used for happy-path testing.
   *
   * @return A populated FlashCardsConfiguration object, setup for validation happy-path.
   */
  public static FlashCardsConfiguration flashCardsConfiguration() {
    return flashCardsConfigurationBuilder().build();
  }

  /**
   * Construct and populate a valid FlashCardGroup.Builder that can be used for happy-path testing.
   *
   * @return A populated FlashCardGroup.Builder object, setup for validation happy-path.
   */
  public static FlashCardGroup.Builder flashCardGroupBuilder() {
    Map<String, String> prompts = new TreeMap<>();
    prompts.put("key", "value");
    return FlashCardGroup.builder().prompts(prompts).maxDuration(Duration.ofSeconds(30));
  }

  /**
   * Construct and populate a valid FlashCardGroup that can be used for happy-path testing.
   *
   * @return A populated FlashCardGroup object, setup for validation happy-path.
   */
  public static FlashCardGroup flashCardGroup() {
    return flashCardGroupBuilder().build();
  }
}
