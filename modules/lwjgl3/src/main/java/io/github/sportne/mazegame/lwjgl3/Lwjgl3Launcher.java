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
import java.util.Optional;

/** Desktop launcher for Maze Game. */
public final class Lwjgl3Launcher {
  private static final String AUDIO_PROPERTY = "mazeGame.audio";
  private static final String AUDIO_ENVIRONMENT_VARIABLE = "MAZE_GAME_AUDIO";
  private static final String SCREENSHOT_DELAY_ARGUMENT = "--screenshot-delay";
  private static final String SCREENSHOT_ARGUMENT = "--screenshot";

  private Lwjgl3Launcher() {}

  public static void main(String[] args) {
    createApplication(args);
  }

  private static Lwjgl3Application createApplication(String[] args) {
    return new Lwjgl3Application(
        new MazeGame(screenshotCapture(args).orElse(null)), defaultConfiguration(args));
  }

  static Lwjgl3ApplicationConfiguration defaultConfiguration(String... args) {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle(MazeGame.title());
    configuration.setWindowedMode(1280, 720);
    configuration.useVsync(true);
    configuration.setForegroundFPS(60);
    configuration.disableAudio(!audioEnabled(args));
    configuration.setWindowListener(closeThroughApplicationExit());
    return configuration;
  }

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

  private static Duration secondsToDuration(String value) {
    return Duration.ofMillis(Math.round(Double.parseDouble(value) * 1000.0D));
  }

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
}
