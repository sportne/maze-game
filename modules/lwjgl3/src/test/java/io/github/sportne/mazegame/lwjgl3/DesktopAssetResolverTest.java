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
    Files.createDirectory(temporaryDirectory.resolve("processed"));
    Files.createFile(temporaryDirectory.resolve("processed/classic-mouse.png"));

    assertEquals(
        Path.of("processed", "classic-mouse.png"),
        DesktopAssetResolver.resolvedPath(
            "processed/classic-mouse.png", null, temporaryDirectory.toString()));
  }

  @Test
  void projectWorkingDirectoryUsesAssetsFallback() {
    assertEquals(
        Path.of("assets", "processed", "classic-mouse.png"),
        DesktopAssetResolver.resolvedPath(
            "processed/classic-mouse.png", null, temporaryDirectory.toString()));
  }

  @Test
  void blankAssetsDirectoryUsesNormalFallback() {
    assertEquals(
        Path.of("assets", "processed", "classic-mouse.png"),
        DesktopAssetResolver.resolvedPath(
            "processed/classic-mouse.png", " ", temporaryDirectory.toString()));
  }
}
