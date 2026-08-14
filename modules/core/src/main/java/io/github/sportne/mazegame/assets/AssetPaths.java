package io.github.sportne.mazegame.assets;

/** Browser-safe asset-relative names used by Maze Game. */
public final class AssetPaths {
  /** Asset-relative path for background music. */
  private static final String BACKGROUND_MUSIC_PATH = "audio/exploreMaze_T1.mp3";

  /** Asset-relative path for the processed classic mouse sprite sheet. */
  private static final String CLASSIC_MOUSE_SPRITE_SHEET_PATH = "processed/classic-mouse.png";

  /** Asset-relative path for the processed basic character sprite sheet. */
  private static final String BASIC_CHARACTER_SPRITE_SHEET_PATH = "processed/basic-characters.png";

  /** Asset-relative path for the processed goal sprite sheet. */
  private static final String GOAL_SPRITE_SHEET_PATH = "processed/goals.png";

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
   * Returns the asset-relative classic mouse sprite-sheet path.
   *
   * @return default sprite sheet asset path
   */
  public static String classicMouseSpriteSheetPath() {
    return CLASSIC_MOUSE_SPRITE_SHEET_PATH;
  }

  /**
   * Returns the asset-relative basic-character sprite-sheet path.
   *
   * @return Scout sprite asset path
   */
  public static String basicCharacterSpriteSheetPath() {
    return BASIC_CHARACTER_SPRITE_SHEET_PATH;
  }

  /**
   * Returns the asset-relative goal sprite-sheet path.
   *
   * @return processed goal sprite-sheet asset path
   */
  public static String goalSpriteSheetPath() {
    return GOAL_SPRITE_SHEET_PATH;
  }
}
