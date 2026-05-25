package io.github.sportne.mazegame.model;

/**
 * Terminal status for a mouse run.
 *
 * <p>{@link #RUNNING} is the only non-terminal status. The other values end the run and send the
 * game to the result phase.
 */
public enum MouseRunStatus {
  /** The mouse can still move and has not reached the cheese or timed out. */
  RUNNING,

  /** The mouse reached the cheese before the maximum solve time elapsed. */
  REACHED_CHEESE,

  /** The maximum solve time elapsed before the mouse reached the cheese. */
  TIMED_OUT
}
