package io.github.sportne.mazegame.runtime;

import com.badlogic.gdx.files.FileHandle;

/** Resolves one asset-relative path for the active platform. */
@FunctionalInterface
public interface AssetResolver {
  /**
   * Resolves an asset-relative path.
   *
   * @param assetPath asset-relative path
   * @return file handle suitable for the active libGDX backend
   */
  FileHandle resolve(String assetPath);
}
