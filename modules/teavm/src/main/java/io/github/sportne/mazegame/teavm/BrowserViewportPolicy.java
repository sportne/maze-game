package io.github.sportne.mazegame.teavm;

/** Defines the smallest landscape viewport where the game controls remain comfortably usable. */
final class BrowserViewportPolicy {
  static final int MINIMUM_WIDTH = 640;
  static final int MINIMUM_HEIGHT = 360;

  private BrowserViewportPolicy() {}

  /** Returns whether the browser should ask the player to resize or rotate their device. */
  static boolean requiresGuidance(int width, int height) {
    return width < MINIMUM_WIDTH || height < MINIMUM_HEIGHT || height > width;
  }
}
