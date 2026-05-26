package io.github.sportne.mazegame.debug;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/**
 * One-frame screenshot capture request for debug and smoke-test runs.
 *
 * @param outputPath file where the PNG should be written
 * @param delay game time to wait before capturing the frame
 */
public record ScreenshotCapture(Path outputPath, Duration delay) {
  /**
   * Creates an immediate screenshot request.
   *
   * @param outputPath file where the PNG should be written
   */
  public ScreenshotCapture(Path outputPath) {
    this(outputPath, Duration.ZERO);
  }

  /**
   * Creates a validated screenshot request.
   *
   * @throws IllegalArgumentException when the delay is negative
   */
  public ScreenshotCapture {
    Objects.requireNonNull(outputPath, "outputPath must not be null");
    Objects.requireNonNull(delay, "delay must not be null");
    if (delay.isNegative()) {
      throw new IllegalArgumentException("delay must not be negative");
    }
  }
}
