package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class ScreenshotCaptureTest {
  @Test
  void rejectsMissingOutputPath() {
    assertThrows(NullPointerException.class, () -> new ScreenshotCapture(null));
  }

  @Test
  void storesOutputPath() {
    Path outputPath = Path.of("build/screenshots/game.png");

    assertEquals(outputPath, new ScreenshotCapture(outputPath).outputPath());
  }

  @Test
  void defaultsToImmediateCapture() {
    assertEquals(Duration.ZERO, new ScreenshotCapture(Path.of("game.png")).delay());
  }

  @Test
  void storesCaptureDelay() {
    Duration delay = Duration.ofSeconds(3);

    assertEquals(delay, new ScreenshotCapture(Path.of("game.png"), delay).delay());
  }

  @Test
  void rejectsInvalidDelay() {
    Path outputPath = Path.of("game.png");

    assertThrows(NullPointerException.class, () -> new ScreenshotCapture(outputPath, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ScreenshotCapture(outputPath, Duration.ofMillis(-1)));
  }
}
