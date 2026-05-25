package io.github.sportne.mazegame.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import io.github.sportne.mazegame.MazeGame;
import io.github.sportne.mazegame.ScreenshotCapture;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Desktop launcher for Maze Game.
 *
 * <p>This class is the boundary between command-line options and the libGDX LWJGL3 backend. It
 * parses debug options, configures the window, configures audio behavior, and installs a close
 * listener that routes window-close requests through {@link com.badlogic.gdx.Application#exit()}.
 */
public final class Lwjgl3Launcher {
  /** Java system property used to enable or disable audio. */
  private static final String AUDIO_PROPERTY = "mazeGame.audio";

  /** Default desktop window width in pixels. */
  private static final int DEFAULT_WINDOW_WIDTH = 1280;

  /** Default desktop window height in pixels. */
  private static final int DEFAULT_WINDOW_HEIGHT = 720;

  /** Environment variable used to enable or disable audio. */
  private static final String AUDIO_ENVIRONMENT_VARIABLE = "MAZE_GAME_AUDIO";

  /** Command-line flag for delaying a screenshot capture by seconds. */
  private static final String SCREENSHOT_DELAY_ARGUMENT = "--screenshot-delay";

  /** Command-line flag for writing a one-frame screenshot PNG. */
  private static final String SCREENSHOT_ARGUMENT = "--screenshot";

  /** Command-line flag for overriding the desktop window size as WIDTHxHEIGHT. */
  private static final String WINDOW_SIZE_ARGUMENT = "--window-size";

  /** Prevents instantiation of this static launcher. */
  private Lwjgl3Launcher() {}

  /**
   * Launches the desktop application.
   *
   * @param args command-line arguments such as {@code --no-audio} or {@code --screenshot=path}
   */
  public static void main(String[] args) {
    createApplication(args);
  }

  /**
   * Creates the LWJGL3 application.
   *
   * @param args command-line arguments used for game and window configuration
   * @return the running libGDX application
   */
  private static Lwjgl3Application createApplication(String[] args) {
    return new Lwjgl3Application(
        new MazeGame(screenshotCapture(args).orElse(null)), defaultConfiguration(args));
  }

  /**
   * Builds the default desktop window configuration.
   *
   * @param args command-line arguments that can affect audio configuration
   * @return configured LWJGL3 application settings
   */
  static Lwjgl3ApplicationConfiguration defaultConfiguration(String... args) {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    WindowSize windowSize =
        windowSize(args).orElse(new WindowSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
    configuration.setTitle(MazeGame.title());
    configuration.setWindowedMode(windowSize.width(), windowSize.height());
    configuration.useVsync(true);
    configuration.setForegroundFPS(60);
    configuration.disableAudio(!audioEnabled(args));
    configuration.setWindowListener(closeThroughApplicationExit());
    return configuration;
  }

  /**
   * Parses an optional window size override.
   *
   * @param args command-line arguments
   * @return requested window size when provided
   */
  static Optional<WindowSize> windowSize(String... args) {
    for (int index = 0; index < args.length; index++) {
      String argument = args[index];
      if (argument.startsWith(WINDOW_SIZE_ARGUMENT + "=")) {
        return Optional.of(
            parseWindowSize(argument.substring((WINDOW_SIZE_ARGUMENT + "=").length())));
      }
      if (WINDOW_SIZE_ARGUMENT.equals(argument) && index + 1 < args.length) {
        return Optional.of(parseWindowSize(args[index + 1]));
      }
    }
    return Optional.empty();
  }

  /**
   * Parses a WIDTHxHEIGHT window-size value.
   *
   * @param value command-line value to parse
   * @return validated window size
   */
  private static WindowSize parseWindowSize(String value) {
    String[] dimensions = value.toLowerCase(Locale.ROOT).split("x", -1);
    if (dimensions.length != 2) {
      throw new IllegalArgumentException("window size must use WIDTHxHEIGHT");
    }
    int width = Integer.parseInt(dimensions[0]);
    int height = Integer.parseInt(dimensions[1]);
    return new WindowSize(width, height);
  }

  /**
   * Returns whether audio should be enabled for this run.
   *
   * <p>Command-line flags take precedence, followed by the {@code mazeGame.audio} system property
   * and then the {@code MAZE_GAME_AUDIO} environment variable.
   *
   * @param args command-line arguments
   * @return true when audio should be enabled
   */
  static boolean audioEnabled(String... args) {
    if (Arrays.asList(args).contains("--no-audio")) {
      return false;
    }
    if (Arrays.asList(args).contains("--audio")) {
      return true;
    }
    String audioProperty = System.getProperty(AUDIO_PROPERTY);
    if (audioProperty != null) {
      return Boolean.parseBoolean(audioProperty);
    }
    String audioEnvironment = System.getenv(AUDIO_ENVIRONMENT_VARIABLE);
    if (audioEnvironment != null) {
      return Boolean.parseBoolean(audioEnvironment);
    }
    return true;
  }

  /**
   * Parses screenshot capture arguments.
   *
   * @param args command-line arguments
   * @return screenshot request when a path is provided
   */
  static Optional<ScreenshotCapture> screenshotCapture(String... args) {
    Optional<Duration> delay = screenshotDelay(args);
    for (int index = 0; index < args.length; index++) {
      String argument = args[index];
      if (argument.startsWith(SCREENSHOT_ARGUMENT + "=")) {
        String path = argument.substring((SCREENSHOT_ARGUMENT + "=").length());
        return Optional.of(new ScreenshotCapture(Path.of(path), delay.orElse(Duration.ZERO)));
      }
      if (SCREENSHOT_ARGUMENT.equals(argument) && index + 1 < args.length) {
        return Optional.of(
            new ScreenshotCapture(Path.of(args[index + 1]), delay.orElse(Duration.ZERO)));
      }
    }
    return Optional.empty();
  }

  /**
   * Parses screenshot delay arguments.
   *
   * @param args command-line arguments
   * @return requested delay duration when provided
   */
  static Optional<Duration> screenshotDelay(String... args) {
    for (int index = 0; index < args.length; index++) {
      String argument = args[index];
      if (argument.startsWith(SCREENSHOT_DELAY_ARGUMENT + "=")) {
        return Optional.of(
            secondsToDuration(argument.substring((SCREENSHOT_DELAY_ARGUMENT + "=").length())));
      }
      if (SCREENSHOT_DELAY_ARGUMENT.equals(argument) && index + 1 < args.length) {
        return Optional.of(secondsToDuration(args[index + 1]));
      }
    }
    return Optional.empty();
  }

  /**
   * Converts a decimal seconds string into a duration.
   *
   * @param value seconds value such as {@code 30.4}
   * @return duration rounded to the nearest millisecond
   */
  private static Duration secondsToDuration(String value) {
    return Duration.ofMillis(Math.round(Double.parseDouble(value) * 1000.0D));
  }

  /**
   * Creates the close listener used by the desktop window.
   *
   * @return listener that requests a libGDX application exit and cancels native immediate close
   */
  static Lwjgl3WindowListener closeThroughApplicationExit() {
    return new Lwjgl3WindowListener() {
      @Override
      public void created(Lwjgl3Window window) {}

      @Override
      public void iconified(boolean isIconified) {}

      @Override
      public void maximized(boolean isMaximized) {}

      @Override
      public void focusLost() {}

      @Override
      public void focusGained() {}

      @Override
      public boolean closeRequested() {
        if (Gdx.app != null) {
          Gdx.app.exit();
        }
        return false;
      }

      @Override
      public void filesDropped(String[] files) {}

      @Override
      public void refreshRequested() {}
    };
  }

  /**
   * Desktop window dimensions requested at launch.
   *
   * @param width window width in pixels
   * @param height window height in pixels
   */
  record WindowSize(int width, int height) {
    /**
     * Creates validated window dimensions.
     *
     * @throws IllegalArgumentException when width or height is not positive
     */
    WindowSize {
      if (width <= 0 || height <= 0) {
        throw new IllegalArgumentException("window dimensions must be positive");
      }
    }
  }
}
