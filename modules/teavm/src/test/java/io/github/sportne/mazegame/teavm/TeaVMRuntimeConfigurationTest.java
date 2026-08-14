package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

final class TeaVMRuntimeConfigurationTest {
  @Test
  void createsBrowserCapabilitiesAroundAssetResolver() {
    FileHandle expectedAsset = new FileHandle("processed/classic-mouse.png");
    MazeGameRuntimeConfiguration configuration =
        TeaVMRuntimeConfiguration.create(ignoredPath -> expectedAsset, ignoredDelta -> {});

    assertSame(expectedAsset, configuration.assetResolver().resolve("processed/classic-mouse.png"));
    assertFalse(configuration.quitAvailable());
    assertTrue(configuration.audioAvailable());
    assertTrue(configuration.audioRequiresUserGesture());
    assertDoesNotThrow(() -> configuration.afterRenderHook().afterRender(0.25F));
    assertDoesNotThrow(configuration.exitAction()::run);
  }

  @Test
  void acceptsBrowserPageStateAfterRenderHook() {
    AtomicBoolean rendered = new AtomicBoolean(false);
    MazeGameRuntimeConfiguration configuration =
        TeaVMRuntimeConfiguration.create(ignoredPath -> null, ignoredDelta -> rendered.set(true));

    configuration.afterRenderHook().afterRender(0.25F);

    assertTrue(rendered.get());
  }
}
