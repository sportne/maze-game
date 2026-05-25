package io.github.sportne.mazegame;

/**
 * Rectangular button bounds in libGDX bottom-left screen coordinates.
 *
 * @param x left edge of the button in pixels
 * @param y bottom edge of the button in pixels
 * @param width button width in pixels
 * @param height button height in pixels
 */
record ButtonBounds(float x, float y, float width, float height) {
  /**
   * Returns whether a bottom-left screen coordinate falls inside this rectangle.
   *
   * @param pointX x coordinate from the left edge of the window
   * @param pointY y coordinate from the bottom edge of the window
   * @return true when the point is on or inside the rectangle boundary
   */
  boolean contains(float pointX, float pointY) {
    return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
  }
}
