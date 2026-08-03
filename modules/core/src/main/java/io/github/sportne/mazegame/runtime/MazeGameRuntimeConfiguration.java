package io.github.sportne.mazegame.runtime;

import com.badlogic.gdx.Gdx;
import java.util.Objects;

/**
 * Immutable platform capabilities and services used by {@code MazeGame}.
 *
 * @param assetResolver asset lookup for the active platform
 * @param afterRenderHook optional work performed after each rendered frame
 * @param exitAction action invoked by the Quit command
 * @param quitAvailable whether the platform offers a Quit command
 * @param audioAvailable whether the platform can play audio
 * @param audioRequiresUserGesture whether playback must wait for the first user interaction
 */
public record MazeGameRuntimeConfiguration(
    AssetResolver assetResolver,
    AfterRenderHook afterRenderHook,
    Runnable exitAction,
    boolean quitAvailable,
    boolean audioAvailable,
    boolean audioRequiresUserGesture) {
  /** Validates required runtime services. */
  public MazeGameRuntimeConfiguration {
    Objects.requireNonNull(assetResolver, "assetResolver");
    Objects.requireNonNull(afterRenderHook, "afterRenderHook");
    Objects.requireNonNull(exitAction, "exitAction");
  }

  /**
   * Creates the normal desktop/test configuration around an asset resolver.
   *
   * @param assetResolver asset lookup for the active runtime
   * @return configuration with Quit and immediate audio enabled
   */
  public static MazeGameRuntimeConfiguration defaults(AssetResolver assetResolver) {
    return new MazeGameRuntimeConfiguration(
        assetResolver,
        ignoredDelta -> {},
        () -> {
          if (Gdx.app != null) {
            Gdx.app.exit();
          }
        },
        true,
        true,
        false);
  }
}
