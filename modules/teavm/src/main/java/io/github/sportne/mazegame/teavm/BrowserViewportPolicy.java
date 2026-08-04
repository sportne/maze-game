package io.github.sportne.mazegame.teavm;

/** Defines the smallest viewport where the scaled game controls remain usable. */
final class BrowserViewportPolicy {
  static final int MINIMUM_LONG_SIDE = 568;
  static final int MINIMUM_SHORT_SIDE = 270;

  private BrowserViewportPolicy() {}

  /** Returns whether the browser should ask the player for more usable screen space. */
  static boolean requiresGuidance(int width, int height) {
    int longSide = Math.max(width, height);
    int shortSide = Math.min(width, height);
    return longSide < MINIMUM_LONG_SIDE || shortSide < MINIMUM_SHORT_SIDE;
  }
}
