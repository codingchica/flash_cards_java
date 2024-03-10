package com.codingchica.flashcards;

import com.codingchica.flashcards.api.exceptionmappers.RenderableExceptionMapper;
import com.codingchica.flashcards.api.resources.QuizResource;
import com.codingchica.flashcards.core.config.FlashCardsConfiguration;
import com.codingchica.flashcards.core.mappers.QuizMapper;
import com.codingchica.flashcards.core.mappers.QuizMapperImpl;
import com.codingchica.flashcards.service.QuizService;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import org.apache.commons.lang3.ArrayUtils;

/** The main DropWizard application / controller. */
public class FlashCardsApplication extends Application<FlashCardsConfiguration> {
  /**
   * Entry point from the command line when starting up the DropWizard application.
   *
   * @param args Command line arguments.
   * @throws Exception If the application is unable to start up.
   */
  public static void main(final String[] args) throws Exception {
    String[] arguments = ArrayUtils.nullToEmpty(args);
    new FlashCardsApplication().run(arguments);
  }

  /**
   * Retrieve the name of the application. Mostly used for the command-line interface.
   *
   * @return The application name.
   */
  @Override
  public String getName() {
    return "flashCards";
  }

  /**
   * Initialize the application with the provided bootstrap configuration. This is where you would
   * add bundles, or commands
   *
   * @param bootstrap The configuration to use to bootstrap the application during startup.
   * @see <a
   *     href="https://www.dropwizard.io/en/latest/manual/core.html#man-core-bundles">Bundles</a>
   * @see <a
   *     href="https://www.dropwizard.io/en/latest/manual/core.html#man-core-commands">Commands</a>
   */
  @Override
  public void initialize(final Bootstrap<FlashCardsConfiguration> bootstrap) {}

  /**
   * Construct a new QuizMapper.
   *
   * @return A QuizMapper instance.
   */
  public QuizMapper quizMapper() {
    return new QuizMapperImpl();
  }

  /**
   * Construct a new UUIDGenerator.
   *
   * @return A UUIDGenerator instance.
   */
  public ObjectIdGenerators.UUIDGenerator uuidGenerator() {
    return new ObjectIdGenerators.UUIDGenerator();
  }

  /**
   * Construct a new QuizService.
   *
   * @param configuration The configuration to use within the QuizService.
   * @return A QuizService instance.
   */
  public QuizService quizService(final FlashCardsConfiguration configuration) {
    return QuizService.builder()
        .flashCardsConfiguration(configuration)
        .quizMapper(quizMapper())
        .uuidGenerator(uuidGenerator())
        .build();
  }

  /**
   * Construct a new QuizResource.
   *
   * @param configuration The configuration to use within the QuizResource.
   * @return A new QuizResource.
   */
  public QuizResource quizResource(final FlashCardsConfiguration configuration) {
    return QuizResource.builder().quizService(quizService(configuration)).build();
  }

  /**
   * Execute the DropWizard application with the specified configuration and environment settings.
   * This is where you would add filters, health checks, health, Jersey providers, Managed Objects,
   * servlets, and tasks.
   *
   * <ul>
   *   <li><a href="https://www.dropwizard.io/en/latest/manual/core.html#man-core-health">Health</a>
   *   <li><a
   *       href="https://www.dropwizard.io/en/latest/manual/core.html#man-core-healthchecks">Health
   *       Checks</a>
   *   <li><a href="https://www.dropwizard.io/en/latest/manual/core.html#man-core-managed">Managed
   *       Objects</a>
   *   <li><a
   *       href="https://www.dropwizard.io/en/latest/manual/core.html#man-core-resources">Resources</a>
   *   <li><a href="https://www.dropwizard.io/en/latest/manual/core.html#man-core-tasks">Tasks</a>
   * </ul>
   *
   * @param configuration POJO representing configuration file provided during application launch.
   * @param environment Environment setup to work within.
   */
  @Override
  public void run(final FlashCardsConfiguration configuration, final Environment environment) {
    JerseyEnvironment jerseyEnvironment = environment.jersey();

    // Resources that will be used by the application.
    jerseyEnvironment.register(quizResource(configuration));

    // Exception mappers
    jerseyEnvironment.register(new RenderableExceptionMapper());
  }
}
