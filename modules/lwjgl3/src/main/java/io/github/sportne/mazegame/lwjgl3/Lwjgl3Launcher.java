package io.github.sportne.mazegame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.sportne.mazegame.MazeGame;
import java.util.Arrays;

/** Desktop launcher for Maze Game. */
public final class Lwjgl3Launcher {
  private static final String AUDIO_PROPERTY = "mazeGame.audio";
  private static final String AUDIO_ENVIRONMENT_VARIABLE = "MAZE_GAME_AUDIO";

  private Lwjgl3Launcher() {}

  public static void main(String[] args) {
    createApplication(args);
  }

  private static Lwjgl3Application createApplication(String[] args) {
    return new Lwjgl3Application(new MazeGame(), defaultConfiguration(args));
  }

  static Lwjgl3ApplicationConfiguration defaultConfiguration(String... args) {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle(MazeGame.title());
    configuration.setWindowedMode(1280, 720);
    configuration.useVsync(true);
    configuration.setForegroundFPS(60);
    configuration.disableAudio(!audioEnabled(args));
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
}
