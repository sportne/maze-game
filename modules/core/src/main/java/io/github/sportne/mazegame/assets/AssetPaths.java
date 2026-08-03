package io.github.sportne.mazegame.assets;

/** Browser-safe asset-relative names used by Maze Game. */
public final class AssetPaths {
  /** Asset-relative path for background music. */
  private static final String BACKGROUND_MUSIC_PATH = "audio/exploreMaze_T1.mp3";

  /** Asset-relative path for the mouse and cheese sprite sheet. */
  private static final String SPRITE_SHEET_PATH = "mouse-sprites.png";

  /** Prevents instantiation of this static path helper. */
  private AssetPaths() {}

  /**
   * Returns the asset-relative background music path.
   *
   * @return default music asset path
   */
  public static String backgroundMusicPath() {
    return BACKGROUND_MUSIC_PATH;
  }

  /**
   * Returns the asset-relative sprite sheet path.
   *
   * @return default sprite sheet asset path
   */
  public static String spriteSheetPath() {
    return SPRITE_SHEET_PATH;
  }
}
