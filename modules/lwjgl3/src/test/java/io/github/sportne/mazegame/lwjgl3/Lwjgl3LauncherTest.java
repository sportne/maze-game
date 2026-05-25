package io.github.sportne.mazegame.lwjgl3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Duration;
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
  void screenshotCaptureCanBeRequestedWithEqualsArgument() {
    assertTrue(
        Lwjgl3Launcher.screenshotCapture("--screenshot=build/screenshots/game.png").isPresent());
    assertEquals(
        Path.of("build/screenshots/game.png"),
        Lwjgl3Launcher.screenshotCapture("--screenshot=build/screenshots/game.png")
            .orElseThrow()
            .outputPath());
  }

  @Test
  void screenshotCaptureCanBeRequestedWithSeparatePathArgument() {
    assertTrue(
        Lwjgl3Launcher.screenshotCapture("--screenshot", "build/screenshots/game.png").isPresent());
    assertEquals(
        Path.of("build/screenshots/game.png"),
        Lwjgl3Launcher.screenshotCapture("--screenshot", "build/screenshots/game.png")
            .orElseThrow()
            .outputPath());
  }

  @Test
  void screenshotCaptureIsEmptyWhenNoPathIsProvided() {
    assertTrue(Lwjgl3Launcher.screenshotCapture("--screenshot").isEmpty());
  }

  @Test
  void screenshotCaptureUsesRequestedDelay() {
    assertEquals(
        Duration.ofMillis(30250),
        Lwjgl3Launcher.screenshotCapture("--screenshot=game.png", "--screenshot-delay=30.25")
            .orElseThrow()
            .delay());
  }

  @Test
  void screenshotDelayCanBeRequestedWithSeparateValueArgument() {
    assertEquals(
        Duration.ofSeconds(2),
        Lwjgl3Launcher.screenshotDelay("--screenshot-delay", "2").orElseThrow());
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

  @Test
  void desktopLauncherInstallsWindowCloseListener() throws ReflectiveOperationException {
    assertNotNull(windowListener(Lwjgl3Launcher.defaultConfiguration()));
  }

  @Test
  void closeListenerCancelsNativeCloseAndUsesApplicationExit() {
    assertFalse(Lwjgl3Launcher.closeThroughApplicationExit().closeRequested());
  }

  private static boolean disableAudio(Lwjgl3ApplicationConfiguration configuration)
      throws ReflectiveOperationException {
    Field disableAudio = configuration.getClass().getDeclaredField("disableAudio");
    disableAudio.setAccessible(true);
    return disableAudio.getBoolean(configuration);
  }

  private static Lwjgl3WindowListener windowListener(Lwjgl3ApplicationConfiguration configuration)
      throws ReflectiveOperationException {
    Field windowListener = Lwjgl3WindowConfiguration.class.getDeclaredField("windowListener");
    windowListener.setAccessible(true);
    return (Lwjgl3WindowListener) windowListener.get(configuration);
  }
}
