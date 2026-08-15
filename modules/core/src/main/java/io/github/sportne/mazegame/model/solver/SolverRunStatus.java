package io.github.sportne.mazegame.model.solver;

/**
 * Terminal status for a solver run.
 *
 * <p>{@link #RUNNING} is the only non-terminal status. The other values end the run and send the
 * game to the result phase.
 */
public enum SolverRunStatus {
  /** The solver can still move and has not reached the cheese or timed out. */
  RUNNING,

  /** The solver reached the cheese before the maximum solve time elapsed. */
  REACHED_CHEESE,

  /** The maximum solve time elapsed before the solver reached the cheese. */
  TIMED_OUT
}
