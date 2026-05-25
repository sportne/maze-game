package io.github.sportne.mazegame;

/** Rectangular button bounds in libGDX bottom-left screen coordinates. */
record ButtonBounds(float x, float y, float width, float height) {
  boolean contains(float pointX, float pointY) {
    return pointX >= x && pointX <= x + width && pointY >= y && pointY <= y + height;
  }
}
