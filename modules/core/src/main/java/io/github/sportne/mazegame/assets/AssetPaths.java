package io.github.sportne.mazegame.assets;

import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves Maze Game asset paths across packaged, assets-directory, and project layouts. */
public final class AssetPaths {
  /** Environment variable that can point the app at an external asset directory. */
  public static final String ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE = "MAZE_GAME_ASSETS_DIR";

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
   * Resolves the background music path for the current runtime environment.
   *
   * @param assetsDirectory optional explicit assets directory
   * @param userDirectory process working directory
   * @return absolute, asset-relative, or project-relative path to the music file
   */
  public static String backgroundMusicPath(String assetsDirectory, String userDirectory) {
    return resolve(BACKGROUND_MUSIC_PATH, assetsDirectory, userDirectory);
  }

  /**
   * Returns the asset-relative sprite sheet path.
   *
   * @return default sprite sheet asset path
   */
  public static String spriteSheetPath() {
    return SPRITE_SHEET_PATH;
  }

  /**
   * Resolves the sprite sheet path for the current runtime environment.
   *
   * @param assetsDirectory optional explicit assets directory
   * @param userDirectory process working directory
   * @return absolute, asset-relative, or project-relative path to the sprite sheet
   */
  public static String spriteSheetPath(String assetsDirectory, String userDirectory) {
    return resolve(SPRITE_SHEET_PATH, assetsDirectory, userDirectory);
  }

  /**
   * Resolves any asset-relative path for the current desktop-compatible runtime environment.
   *
   * @param assetPath asset-relative path
   * @param assetsDirectory optional explicit assets directory
   * @param userDirectory process working directory
   * @return absolute, asset-relative, or project-relative path to the asset
   */
  public static String resolve(String assetPath, String assetsDirectory, String userDirectory) {
    if (assetsDirectory != null && !assetsDirectory.isBlank()) {
      return Path.of(assetsDirectory, assetPath).toString();
    }
    if (Files.exists(Path.of(userDirectory, assetPath))) {
      return assetPath;
    }
    return Path.of("assets", assetPath).toString();
  }
}
