package io.github.sportne.mazegame.lwjgl3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class DesktopAssetResolverTest {
  @TempDir private Path temporaryDirectory;

  @Test
  void explicitAssetsDirectoryTakesPrecedence() {
    assertEquals(
        temporaryDirectory.resolve("audio/music.mp3"),
        DesktopAssetResolver.resolvedPath(
            "audio/music.mp3", temporaryDirectory.toString(), temporaryDirectory.toString()));
  }

  @Test
  void assetsWorkingDirectoryUsesRelativeName() throws IOException {
    Files.createFile(temporaryDirectory.resolve("mouse-sprites.png"));

    assertEquals(
        Path.of("mouse-sprites.png"),
        DesktopAssetResolver.resolvedPath(
            "mouse-sprites.png", null, temporaryDirectory.toString()));
  }

  @Test
  void projectWorkingDirectoryUsesAssetsFallback() {
    assertEquals(
        Path.of("assets", "mouse-sprites.png"),
        DesktopAssetResolver.resolvedPath(
            "mouse-sprites.png", null, temporaryDirectory.toString()));
  }

  @Test
  void blankAssetsDirectoryUsesNormalFallback() {
    assertEquals(
        Path.of("assets", "mouse-sprites.png"),
        DesktopAssetResolver.resolvedPath("mouse-sprites.png", " ", temporaryDirectory.toString()));
  }
}
