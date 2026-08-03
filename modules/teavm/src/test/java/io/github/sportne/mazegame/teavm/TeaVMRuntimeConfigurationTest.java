package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import org.junit.jupiter.api.Test;

final class TeaVMRuntimeConfigurationTest {
  @Test
  void createsBrowserCapabilitiesAroundAssetResolver() {
    FileHandle expectedAsset = new FileHandle("mouse-sprites.png");
    MazeGameRuntimeConfiguration configuration =
        TeaVMRuntimeConfiguration.create(ignoredPath -> expectedAsset);

    assertSame(expectedAsset, configuration.assetResolver().resolve("mouse-sprites.png"));
    assertFalse(configuration.quitAvailable());
    assertTrue(configuration.audioAvailable());
    assertTrue(configuration.audioRequiresUserGesture());
    assertDoesNotThrow(() -> configuration.afterRenderHook().afterRender(0.25F));
    assertDoesNotThrow(configuration.exitAction()::run);
  }
}
