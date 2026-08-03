package io.github.sportne.mazegame.teavm;

import io.github.sportne.mazegame.runtime.AssetResolver;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;

/** Creates portable game services with browser capabilities. */
final class TeaVMRuntimeConfiguration {
  private TeaVMRuntimeConfiguration() {}

  /**
   * Creates the initial JavaScript runtime configuration.
   *
   * <p>Audio stays unavailable until WEB-06 adds user-gesture startup handling.
   *
   * @param assetResolver browser-internal asset lookup
   * @return game runtime configured for a browser page
   */
  static MazeGameRuntimeConfiguration create(AssetResolver assetResolver) {
    return new MazeGameRuntimeConfiguration(
        assetResolver, ignoredDelta -> {}, () -> {}, false, false, true);
  }
}
