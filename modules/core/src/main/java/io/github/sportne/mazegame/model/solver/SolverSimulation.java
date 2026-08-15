package io.github.sportne.mazegame.model.solver;

import java.time.Duration;
import java.util.Optional;

/** Timed solver movement used by a game session. */
public interface SolverSimulation {
  /**
   * Advances the simulation by the given time.
   *
   * @param deltaTime amount of time to add to the run
   * @return the updated run snapshot
   */
  SolverRunResult update(Duration deltaTime);

  /**
   * Returns the current simulation result.
   *
   * @return immutable snapshot of position, time, move count, and status
   */
  SolverRunResult result();

  /**
   * Returns the direction of the most recent actual move.
   *
   * @return movement direction, or empty before the first move
   */
  default Optional<CardinalDirection> lastDirection() {
    return Optional.empty();
  }
}
