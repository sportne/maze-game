package io.github.sportne.mazegame.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.sportne.mazegame.runtime.AfterRenderHook;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/** Coordinates one delayed desktop framebuffer capture. */
final class ScreenshotCapture implements AfterRenderHook {
  private final Path outputPath;
  private final Duration delay;
  private final Consumer<Path> screenshotWriter;
  private boolean captured;
  private float elapsedSeconds;

  /**
   * Creates a capture using the desktop PNG writer.
   *
   * @param outputPath file where the PNG should be written
   * @param delay game time to wait before capturing the frame
   */
  ScreenshotCapture(Path outputPath, Duration delay) {
    this(outputPath, delay, ScreenshotCapture::writeFramebufferPng);
  }

  ScreenshotCapture(Path outputPath, Duration delay, Consumer<Path> screenshotWriter) {
    this.outputPath = Objects.requireNonNull(outputPath, "outputPath");
    this.delay = Objects.requireNonNull(delay, "delay");
    this.screenshotWriter = Objects.requireNonNull(screenshotWriter, "screenshotWriter");
    if (delay.isNegative()) {
      throw new IllegalArgumentException("delay must not be negative");
    }
  }

  Path outputPath() {
    return outputPath;
  }

  Duration delay() {
    return delay;
  }

  @Override
  public void afterRender(float deltaSeconds) {
    if (captured) {
      return;
    }
    elapsedSeconds += Math.max(0.0F, deltaSeconds);
    if (elapsedSeconds < delay.toMillis() / 1000.0F) {
      return;
    }
    captured = true;
    screenshotWriter.accept(outputPath.toAbsolutePath());
  }

  private static void writeFramebufferPng(Path outputPath) {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      int width = Gdx.graphics.getWidth();
      int height = Gdx.graphics.getHeight();
      byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, width, height, true);
      Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
      BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
      try {
        PixmapIO.writePNG(Gdx.files.absolute(outputPath.toString()), pixmap);
      } finally {
        pixmap.dispose();
      }
    } catch (IOException exception) {
      throw new GdxRuntimeException("Failed to capture screenshot to " + outputPath, exception);
    }
  }
}
