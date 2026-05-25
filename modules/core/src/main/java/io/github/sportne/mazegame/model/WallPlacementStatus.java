package io.github.sportne.mazegame.model;

/** Status for a wall placement attempt. */
public enum WallPlacementStatus {
  PLACED(true),
  ALREADY_PRESENT(true),
  REJECTED_OUTSIDE_GRID(false),
  REJECTED_PROTECTED_CELL(false),
  REJECTED_BLOCKS_PATH(false);

  private final boolean accepted;

  WallPlacementStatus(boolean accepted) {
    this.accepted = accepted;
  }

  /** Returns whether this status represents an accepted placement. */
  public boolean accepted() {
    return accepted;
  }
}
