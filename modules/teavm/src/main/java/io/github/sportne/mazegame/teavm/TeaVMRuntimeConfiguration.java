package io.github.sportne.mazegame.teavm;

import io.github.sportne.mazegame.runtime.AfterRenderHook;
import io.github.sportne.mazegame.runtime.AssetResolver;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;

/** Creates portable game services with browser capabilities. */
final class TeaVMRuntimeConfiguration {
  private TeaVMRuntimeConfiguration() {}

  /** Creates the browser configuration with a page-state callback after each rendered frame. */
  static MazeGameRuntimeConfiguration create(
      AssetResolver assetResolver, AfterRenderHook afterRenderHook) {
    return new MazeGameRuntimeConfiguration(
        assetResolver, afterRenderHook, () -> {}, false, true, true);
  }
}
