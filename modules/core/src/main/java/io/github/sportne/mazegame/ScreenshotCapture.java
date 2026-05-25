package io.github.sportne.mazegame;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/** One-frame screenshot capture request for debug and smoke-test runs. */
public record ScreenshotCapture(Path outputPath, Duration delay) {
  public ScreenshotCapture(Path outputPath) {
    this(outputPath, Duration.ZERO);
  }

  public ScreenshotCapture {
    Objects.requireNonNull(outputPath, "outputPath must not be null");
    Objects.requireNonNull(delay, "delay must not be null");
    if (delay.isNegative()) {
      throw new IllegalArgumentException("delay must not be negative");
    }
  }
}
