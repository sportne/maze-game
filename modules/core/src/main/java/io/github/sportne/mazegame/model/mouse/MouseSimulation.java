package io.github.sportne.mazegame.model.mouse;

import java.time.Duration;

/** Timed mouse movement used by a game session. */
public interface MouseSimulation {
  /**
   * Advances the simulation by the given time.
   *
   * @param deltaTime amount of time to add to the run
   * @return the updated run snapshot
   */
  MouseRunResult update(Duration deltaTime);

  /**
   * Returns the current simulation result.
   *
   * @return immutable snapshot of position, time, move count, and status
   */
  MouseRunResult result();
}
