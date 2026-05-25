package io.github.sportne.mazegame.layout;

/**
 * Rectangle in the game's bottom-left coordinate space.
 *
 * @param x left edge in pixels
 * @param y bottom edge in pixels
 * @param width rectangle width in pixels
 * @param height rectangle height in pixels
 */
public record ScreenRectangle(float x, float y, float width, float height) {
  /**
   * Returns the right edge.
   *
   * @return x coordinate of the right edge
   */
  public float right() {
    return x + width;
  }

  /**
   * Returns the top edge.
   *
   * @return y coordinate of the top edge
   */
  public float top() {
    return y + height;
  }

  /**
   * Returns whether a point is inside or on the rectangle.
   *
   * @param pointX x coordinate to test
   * @param pointY y coordinate to test
   * @return true when the point is inside this rectangle
   */
  public boolean contains(float pointX, float pointY) {
    return pointX >= x && pointX <= right() && pointY >= y && pointY <= top();
  }

  /**
   * Returns whether this rectangle fits fully inside another rectangle.
   *
   * @param viewport containing rectangle
   * @return true when no edge extends outside the viewport
   */
  public boolean fitsWithin(ScreenRectangle viewport) {
    return x >= viewport.x()
        && y >= viewport.y()
        && right() <= viewport.right()
        && top() <= viewport.top();
  }

  /**
   * Returns whether this rectangle has positive area.
   *
   * @return true when width and height are both greater than zero
   */
  public boolean hasPositiveSize() {
    return width > 0.0F && height > 0.0F;
  }

  /**
   * Returns whether this rectangle overlaps another rectangle with positive area.
   *
   * @param other rectangle to compare against
   * @return true when the rectangles overlap
   */
  public boolean overlaps(ScreenRectangle other) {
    return x < other.right() && right() > other.x() && y < other.top() && top() > other.y();
  }
}
