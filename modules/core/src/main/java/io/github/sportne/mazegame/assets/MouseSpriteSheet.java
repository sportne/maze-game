package io.github.sportne.mazegame.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Defines sprite-sheet regions for the milestone-one mouse and cheese sprites. */
public final class MouseSpriteSheet {
  /** X coordinate of the cheese sprite region. */
  private static final int CHEESE_X = 1168;

  /** Y coordinate of the cheese sprite region. */
  private static final int CHEESE_Y = 819;

  /** Cheese sprite region width. */
  private static final int CHEESE_WIDTH = 186;

  /** Cheese sprite region height. */
  private static final int CHEESE_HEIGHT = 145;

  /** X coordinate of the mouse sprite region. */
  private static final int MOUSE_X = 718;

  /** Y coordinate of the mouse sprite region. */
  private static final int MOUSE_Y = 671;

  /** Mouse sprite region width. */
  private static final int MOUSE_WIDTH = 325;

  /** Mouse sprite region height. */
  private static final int MOUSE_HEIGHT = 416;

  /** Prevents instantiation of this static sprite helper. */
  private MouseSpriteSheet() {}

  /**
   * Creates the cheese sprite region from the loaded sheet.
   *
   * @param spriteSheet loaded sprite-sheet texture
   * @return cheese sprite region
   */
  public static TextureRegion cheese(Texture spriteSheet) {
    return new TextureRegion(spriteSheet, CHEESE_X, CHEESE_Y, CHEESE_WIDTH, CHEESE_HEIGHT);
  }

  /**
   * Creates the mouse sprite region from the loaded sheet.
   *
   * @param spriteSheet loaded sprite-sheet texture
   * @return mouse sprite region
   */
  public static TextureRegion mouse(Texture spriteSheet) {
    return new TextureRegion(spriteSheet, MOUSE_X, MOUSE_Y, MOUSE_WIDTH, MOUSE_HEIGHT);
  }
}
