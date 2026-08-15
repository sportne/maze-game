package io.github.sportne.mazegame.model.solver;

/**
 * Terminal status for a solver run.
 *
 * <p>{@link #RUNNING} is the only non-terminal status. The other values end the run and send the
 * game to the result phase.
 */
public enum SolverRunStatus {
  /** The solver can still move and has not reached its goal or timed out. */
  RUNNING,

  /** The solver reached its goal before the maximum solve time elapsed. */
  REACHED_GOAL,

  /** The maximum solve time elapsed before the solver reached its goal. */
  TIMED_OUT
}
