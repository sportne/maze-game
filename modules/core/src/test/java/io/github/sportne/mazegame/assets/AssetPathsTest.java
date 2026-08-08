package io.github.sportne.mazegame.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class AssetPathsTest {
  @Test
  void backgroundMusicPathPointsAtTheBundledTrack() {
    assertEquals("audio/exploreMaze_T1.mp3", AssetPaths.backgroundMusicPath());
  }

  @Test
  void spriteSheetPathPointsAtTheBundledSheet() {
    assertEquals("mouse-sprites.png", AssetPaths.spriteSheetPath());
  }

  @Test
  void scoutSpritePathPointsAtTheDistinctBundledSprite() {
    assertEquals("scout-mouse.png", AssetPaths.scoutSpritePath());
  }
}
