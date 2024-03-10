package com.codingchica.flashcards.core.mappers;

import com.codingchica.flashcards.core.config.FlashCardGroup;
import com.codingchica.flashcards.core.model.external.Quiz;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * A mapper to translate the internal representation of the quiz in the config to that which should
 * be returned as an external representation of the quiz.
 */
@Mapper
public interface QuizMapper {
  /**
   * Translate the internal representation of the quiz to an external representation.
   *
   * @param flashCardGroup The flashCardGroup from the configuration.
   * @param name The name of the quiz.
   * @return The external representation of the quiz.
   */
  @Mapping(target = "name", source = "name")
  @Mapping(target = "prompts", source = "flashCardGroup.prompts")
  @Mapping(target = "id", source = "uuid")
  @Mapping(target = "dueDateTime", source = "dueDateTime")
  Quiz internalToExternalQuizMapping(
      String name, FlashCardGroup flashCardGroup, UUID uuid, ZonedDateTime dueDateTime);
}
