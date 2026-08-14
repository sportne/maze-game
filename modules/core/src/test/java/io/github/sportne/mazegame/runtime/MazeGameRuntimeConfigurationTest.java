package io.github.sportne.mazegame.runtime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

final class MazeGameRuntimeConfigurationTest {
  @Test
  void defaultsProvideDesktopCapabilitiesAndNoOpServices() {
    FileHandle asset = new FileHandle("asset.png");
    MazeGameRuntimeConfiguration configuration =
        MazeGameRuntimeConfiguration.defaults(ignoredPath -> asset);

    assertSame(asset, configuration.assetResolver().resolve("asset.png"));
    assertTrue(configuration.quitAvailable());
    assertTrue(configuration.audioAvailable());
    assertFalse(configuration.audioRequiresUserGesture());
    assertDoesNotThrow(() -> configuration.afterRenderHook().afterRender(0.25F));
    assertDoesNotThrow(configuration.exitAction()::run);
  }

  @Test
  void injectedServicesRemainSmallAndCallable() {
    AtomicReference<String> resolvedPath = new AtomicReference<>();
    AtomicReference<Float> renderedDelta = new AtomicReference<>();
    AtomicBoolean exitRequested = new AtomicBoolean(false);
    MazeGameRuntimeConfiguration configuration =
        new MazeGameRuntimeConfiguration(
            assetPath -> {
              resolvedPath.set(assetPath);
              return new FileHandle(assetPath);
            },
            renderedDelta::set,
            () -> exitRequested.set(true),
            false,
            true,
            true);

    configuration.assetResolver().resolve("processed/classic-mouse.png");
    configuration.afterRenderHook().afterRender(0.5F);
    configuration.exitAction().run();

    assertEquals("processed/classic-mouse.png", resolvedPath.get());
    assertEquals(0.5F, renderedDelta.get());
    assertTrue(exitRequested.get());
    assertFalse(configuration.quitAvailable());
    assertTrue(configuration.audioRequiresUserGesture());
  }

  @Test
  void requiredServicesRejectNull() {
    AssetResolver assetResolver = FileHandle::new;
    AfterRenderHook afterRenderHook = ignoredDelta -> {};
    Runnable exitAction = () -> {};

    assertThrows(
        NullPointerException.class,
        () ->
            new MazeGameRuntimeConfiguration(null, afterRenderHook, exitAction, true, true, false));
    assertThrows(
        NullPointerException.class,
        () -> new MazeGameRuntimeConfiguration(assetResolver, null, exitAction, true, true, false));
    assertThrows(
        NullPointerException.class,
        () ->
            new MazeGameRuntimeConfiguration(
                assetResolver, afterRenderHook, null, true, true, false));
  }
}
