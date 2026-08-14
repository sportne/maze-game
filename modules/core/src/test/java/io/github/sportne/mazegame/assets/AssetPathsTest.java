package io.github.sportne.mazegame.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class AssetPathsTest {
  @Test
  void backgroundMusicPathPointsAtTheBundledTrack() {
    assertEquals("audio/exploreMaze_T1.mp3", AssetPaths.backgroundMusicPath());
  }

  @Test
  void classicMousePathPointsAtTheProcessedSheet() {
    assertEquals("processed/classic-mouse.png", AssetPaths.classicMouseSpriteSheetPath());
  }

  @Test
  void basicCharacterPathPointsAtTheProcessedSheet() {
    assertEquals("processed/basic-characters.png", AssetPaths.basicCharacterSpriteSheetPath());
  }

  @Test
  void goalPathPointsAtTheProcessedSheet() {
    assertEquals("processed/goals.png", AssetPaths.goalSpriteSheetPath());
  }
}
