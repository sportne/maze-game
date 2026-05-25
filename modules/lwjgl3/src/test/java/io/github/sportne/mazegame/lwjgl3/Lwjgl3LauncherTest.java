package io.github.sportne.mazegame.lwjgl3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

final class Lwjgl3LauncherTest {
  @Test
  void desktopLauncherInitializesAudioByDefault() throws ReflectiveOperationException {
    assertFalse(disableAudio(Lwjgl3Launcher.defaultConfiguration()));
  }

  @Test
  void desktopLauncherCanDisableAudioWhenRequested() throws ReflectiveOperationException {
    assertTrue(disableAudio(Lwjgl3Launcher.defaultConfiguration("--no-audio")));
  }

  @Test
  void audioIsEnabledUnlessDisabled() {
    assertTrue(Lwjgl3Launcher.audioEnabled());
  }

  @Test
  void audioCanBeDisabledWithLaunchArgument() {
    assertFalse(Lwjgl3Launcher.audioEnabled("--no-audio"));
  }

  @Test
  void audioCanBeDisabledWithSystemProperty() {
    String originalValue = System.getProperty("mazeGame.audio");
    try {
      System.setProperty("mazeGame.audio", "false");

      assertFalse(Lwjgl3Launcher.audioEnabled());
    } finally {
      if (originalValue == null) {
        System.clearProperty("mazeGame.audio");
      } else {
        System.setProperty("mazeGame.audio", originalValue);
      }
    }
  }

  private static boolean disableAudio(Lwjgl3ApplicationConfiguration configuration)
      throws ReflectiveOperationException {
    Field disableAudio = configuration.getClass().getDeclaredField("disableAudio");
    disableAudio.setAccessible(true);
    return disableAudio.getBoolean(configuration);
  }
}
