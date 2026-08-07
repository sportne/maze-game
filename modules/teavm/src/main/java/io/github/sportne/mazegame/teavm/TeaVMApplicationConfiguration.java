package io.github.sportne.mazegame.teavm;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;

/** Creates the browser backend configuration without starting a browser application. */
final class TeaVMApplicationConfiguration {
  static final String CANVAS_ID = "canvas";
  static final String STORAGE_PREFIX = "maze-game_";

  private TeaVMApplicationConfiguration() {}

  /**
   * Creates the full-window browser configuration.
   *
   * @return configured gdx-teavm browser backend
   */
  static WebApplicationConfiguration create() {
    WebApplicationConfiguration configuration = new WebApplicationConfiguration(CANVAS_ID);
    configuration.width = 0;
    configuration.height = 0;
    configuration.usePhysicalPixels = false;
    configuration.storagePrefix = STORAGE_PREFIX;
    return configuration;
  }
}
