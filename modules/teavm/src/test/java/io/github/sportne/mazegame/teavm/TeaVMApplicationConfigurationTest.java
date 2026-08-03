package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import org.junit.jupiter.api.Test;

final class TeaVMApplicationConfigurationTest {
  @Test
  void createsFullWindowCanvasWithStableStoragePrefix() {
    WebApplicationConfiguration configuration = TeaVMApplicationConfiguration.create();

    assertEquals("canvas", configuration.canvasID);
    assertEquals(0, configuration.width);
    assertEquals(0, configuration.height);
    assertTrue(configuration.isAutoSizeApplication());
    assertEquals("maze-game_", configuration.storagePrefix);
  }
}
