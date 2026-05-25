package io.github.sportne.mazegame.layout;

/** Type of frontend element represented by a layout contract. */
public enum LayoutElementKind {
  /** Playable maze grid. */
  GRID,

  /** Clickable button or card. */
  BUTTON,

  /** Reserved text drawing area. */
  TEXT_REGION
}
