package io.github.sportne.mazegame.layout;

/** Type of layout contract problem reported by the validator. */
public enum LayoutIssueType {
  /** An element has zero or negative width or height. */
  NON_POSITIVE_SIZE,

  /** A must-fit element extends outside the viewport. */
  OUTSIDE_VIEWPORT,

  /** Two button elements overlap each other. */
  OVERLAPPING_BUTTONS,

  /** A button overlaps the playable grid. */
  BUTTON_OVERLAPS_GRID
}
