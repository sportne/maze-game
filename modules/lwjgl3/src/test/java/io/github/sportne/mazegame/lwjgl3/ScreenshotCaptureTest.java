package io.github.sportne.mazegame.lwjgl3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

final class ScreenshotCaptureTest {
  @Test
  void validatesCaptureRequest() {
    Path outputPath = Path.of("game.png");
    Consumer<Path> writer = ignoredPath -> {};

    assertThrows(
        NullPointerException.class, () -> new ScreenshotCapture(null, Duration.ZERO, writer));
    assertThrows(NullPointerException.class, () -> new ScreenshotCapture(outputPath, null, writer));
    assertThrows(
        NullPointerException.class, () -> new ScreenshotCapture(outputPath, Duration.ZERO, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ScreenshotCapture(outputPath, Duration.ofMillis(-1), writer));
  }

  @Test
  void storesOutputPathAndDelay() {
    Path outputPath = Path.of("build/screenshots/game.png");
    Duration delay = Duration.ofSeconds(3);
    ScreenshotCapture capture = new ScreenshotCapture(outputPath, delay);

    assertEquals(outputPath, capture.outputPath());
    assertEquals(delay, capture.delay());
  }

  @Test
  void writesExactlyOnceAfterDelay() {
    List<Path> writes = new ArrayList<>();
    Path outputPath = Path.of("build/screenshots/game.png");
    ScreenshotCapture capture =
        new ScreenshotCapture(outputPath, Duration.ofSeconds(1), writes::add);

    capture.afterRender(-1.0F);
    capture.afterRender(0.5F);
    assertTrue(writes.isEmpty());

    capture.afterRender(0.5F);
    capture.afterRender(1.0F);

    assertEquals(List.of(outputPath.toAbsolutePath()), writes);
  }
}
