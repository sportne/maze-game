package io.github.sportne.mazegame.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class AssetPathsTest {
  @TempDir private Path temporaryDirectory;

  @Test
  void backgroundMusicPathPointsAtTheBundledTrack() {
    assertEquals("audio/exploreMaze_T1.mp3", AssetPaths.backgroundMusicPath());
  }

  @Test
  void backgroundMusicPathUsesConfiguredAssetsDirectory() {
    assertEquals(
        temporaryDirectory.resolve("audio/exploreMaze_T1.mp3").toString(),
        AssetPaths.backgroundMusicPath(
            temporaryDirectory.toString(), temporaryDirectory.toString()));
  }

  @Test
  void backgroundMusicPathUsesAssetRelativePathFromAssetsWorkingDirectory() throws IOException {
    Files.createDirectories(temporaryDirectory.resolve("audio"));
    Files.createFile(temporaryDirectory.resolve("audio/exploreMaze_T1.mp3"));

    assertEquals(
        "audio/exploreMaze_T1.mp3",
        AssetPaths.backgroundMusicPath(null, temporaryDirectory.toString()));
  }

  @Test
  void backgroundMusicPathFallsBackToProjectRelativeAssetsDirectory() {
    assertEquals(
        "assets/audio/exploreMaze_T1.mp3",
        AssetPaths.backgroundMusicPath(null, temporaryDirectory.toString()));
  }

  @Test
  void spriteSheetPathPointsAtTheBundledSheet() {
    assertEquals("mouse-sprites.png", AssetPaths.spriteSheetPath());
  }

  @Test
  void spriteSheetPathUsesConfiguredAssetsDirectory() {
    assertEquals(
        temporaryDirectory.resolve("mouse-sprites.png").toString(),
        AssetPaths.spriteSheetPath(temporaryDirectory.toString(), temporaryDirectory.toString()));
  }

  @Test
  void spriteSheetPathUsesAssetRelativePathFromAssetsWorkingDirectory() throws IOException {
    Files.createFile(temporaryDirectory.resolve("mouse-sprites.png"));

    assertEquals(
        "mouse-sprites.png", AssetPaths.spriteSheetPath(null, temporaryDirectory.toString()));
  }

  @Test
  void spriteSheetPathFallsBackToProjectRelativeAssetsDirectory() {
    assertEquals(
        "assets/mouse-sprites.png",
        AssetPaths.spriteSheetPath(null, temporaryDirectory.toString()));
  }
}
