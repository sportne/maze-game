package io.github.sportne.mazegame.layout;

/** Whether a layout element is expected to fit inside the viewport. */
public enum LayoutFitPolicy {
  /** The element must be fully inside the viewport. */
  MUST_FIT,

  /** The element may intentionally extend outside the viewport. */
  CAN_OVERFLOW
}
