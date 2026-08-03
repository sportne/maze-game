package io.github.sportne.mazegame.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import io.github.sportne.mazegame.runtime.AssetResolver;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves asset-relative names across the supported desktop launch layouts. */
final class DesktopAssetResolver implements AssetResolver {
  /** Environment variable that can point the app at an external asset directory. */
  static final String ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE = "MAZE_GAME_ASSETS_DIR";

  private final String assetsDirectory;
  private final String userDirectory;

  /** Creates a resolver from the current desktop process environment. */
  DesktopAssetResolver() {
    this(System.getenv(ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE), System.getProperty("user.dir"));
  }

  DesktopAssetResolver(String assetsDirectory, String userDirectory) {
    this.assetsDirectory = assetsDirectory;
    this.userDirectory = userDirectory;
  }

  @Override
  public FileHandle resolve(String assetPath) {
    Path path = resolvedPath(assetPath, assetsDirectory, userDirectory);
    if (path.isAbsolute()) {
      return Gdx.files.absolute(path.toString());
    }
    return Gdx.files.internal(path.toString());
  }

  /**
   * Resolves an asset name without requiring an initialized libGDX backend.
   *
   * @param assetPath asset-relative name
   * @param assetsDirectory optional explicit assets directory
   * @param userDirectory process working directory
   * @return absolute, asset-relative, or project-relative asset path
   */
  static Path resolvedPath(String assetPath, String assetsDirectory, String userDirectory) {
    if (assetsDirectory != null && !assetsDirectory.isBlank()) {
      return Path.of(assetsDirectory, assetPath);
    }
    if (Files.exists(Path.of(userDirectory, assetPath))) {
      return Path.of(assetPath);
    }
    return Path.of("assets", assetPath);
  }
}
