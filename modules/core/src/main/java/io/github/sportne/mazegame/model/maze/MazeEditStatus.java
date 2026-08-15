package io.github.sportne.mazegame.model.maze;

/** Accepted outcome or specific rejection reason for one atomic maze edit. */
public enum MazeEditStatus {
  PLACED(true),
  REPLACED(true),
  REMOVED(true),
  MOVED(true),
  NO_OP(true),
  REJECTED_OUTSIDE_GRID(false),
  REJECTED_PROTECTED_CELL(false),
  REJECTED_FIXED_CELL(false),
  REJECTED_MISSING_SOURCE(false),
  REJECTED_OCCUPIED_DESTINATION(false),
  REJECTED_EXHAUSTED_SUPPLY(false),
  REJECTED_BLOCKS_PATH(false);

  private final boolean accepted;

  MazeEditStatus(boolean accepted) {
    this.accepted = accepted;
  }

  /** Returns whether the edit was accepted. */
  public boolean accepted() {
    return accepted;
  }
}
