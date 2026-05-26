package io.github.sportne.mazegame.model.maze;

/**
 * Status for a wall placement attempt.
 *
 * <p>The UI uses these statuses to decide whether to update the maze normally or show rejected-cell
 * feedback while leaving the existing maze unchanged.
 */
public enum WallPlacementStatus {
  /** A new wall was placed successfully. */
  PLACED(true),

  /** The requested wall was already present, so the maze remains valid and unchanged. */
  ALREADY_PRESENT(true),

  /** The requested position is outside the level grid. */
  REJECTED_OUTSIDE_GRID(false),

  /** The requested position is reserved for the mouse start or cheese. */
  REJECTED_PROTECTED_CELL(false),

  /** Adding the requested wall would disconnect the mouse from the cheese. */
  REJECTED_BLOCKS_PATH(false);

  /** Whether callers should treat this status as a successful placement. */
  private final boolean accepted;

  /**
   * Creates a status with its acceptance flag.
   *
   * @param accepted true when the placement should be considered successful
   */
  WallPlacementStatus(boolean accepted) {
    this.accepted = accepted;
  }

  /**
   * Returns whether this status represents an accepted placement.
   *
   * @return true for placed or already-present walls
   */
  public boolean accepted() {
    return accepted;
  }
}
