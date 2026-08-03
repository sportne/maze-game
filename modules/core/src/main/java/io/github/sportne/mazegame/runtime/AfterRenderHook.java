package io.github.sportne.mazegame.runtime;

/** Optional platform work that runs after a game frame has been rendered. */
@FunctionalInterface
public interface AfterRenderHook {
  /**
   * Runs after one rendered frame.
   *
   * @param deltaSeconds elapsed frame time in seconds
   */
  void afterRender(float deltaSeconds);
}
