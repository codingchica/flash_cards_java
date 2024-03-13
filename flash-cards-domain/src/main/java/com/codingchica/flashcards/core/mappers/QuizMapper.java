package com.codingchica.flashcards.core.mappers;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.model.external.Quiz;

/**
 * A mapper to translate the internal representation of the quiz in the config to that which should
 * be returned as an external representation of the quiz.
 */
public interface QuizMapper {
  /**
   * Translate the internal representation of the quiz to an external representation.
   *
   * @param flashCardGroup The flashCardGroup from the configuration.
   * @param name The name of the quiz.
   * @return The external representation of the quiz.
   */
  Quiz internalToExternalQuizMapping(String name, FlashCardGroup flashCardGroup);
}
