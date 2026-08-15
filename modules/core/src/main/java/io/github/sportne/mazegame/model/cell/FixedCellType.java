package io.github.sportne.mazegame.model.cell;

/** Closed set of cell effects that a level may place permanently on its grid. */
public enum FixedCellType {
  /** An impassable fixed wall. */
  WALL(true, false),

  /** A walkable fixed floor that delays the next solver decision. */
  SLOW_FLOOR(false, true);

  private final boolean blocksMovement;
  private final boolean delaysNextDecision;

  FixedCellType(boolean blocksMovement, boolean delaysNextDecision) {
    this.blocksMovement = blocksMovement;
    this.delaysNextDecision = delaysNextDecision;
  }

  /** Returns whether this cell blocks solver movement and path validation. */
  public boolean blocksMovement() {
    return blocksMovement;
  }

  /** Returns whether entering this cell delays the next solver decision. */
  public boolean delaysNextDecision() {
    return delaysNextDecision;
  }
}
