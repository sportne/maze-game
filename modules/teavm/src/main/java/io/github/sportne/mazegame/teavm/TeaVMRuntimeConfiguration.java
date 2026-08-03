package io.github.sportne.mazegame.teavm;

import io.github.sportne.mazegame.runtime.AssetResolver;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;

/** Creates portable game services with browser capabilities. */
final class TeaVMRuntimeConfiguration {
  private TeaVMRuntimeConfiguration() {}

  /** Creates the browser runtime configuration with gesture-gated audio and no Quit action. */
  static MazeGameRuntimeConfiguration create(AssetResolver assetResolver) {
    return new MazeGameRuntimeConfiguration(
        assetResolver, ignoredDelta -> {}, () -> {}, false, true, true);
  }
}
