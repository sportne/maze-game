package io.github.sportne.mazegame.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Selects game sprites from the normalized runtime sheets produced by the asset pipeline. */
public final class GameSpriteSheets {
  /** Width and height of every normalized runtime frame. */
  private static final int CELL_SIZE = 128;

  /** Column containing the right-facing character pose. */
  private static final int RIGHT_FACING_COLUMN = 3;

  /** Row containing the basic squirrel in the basic-character sheet. */
  private static final int SQUIRREL_ROW = 3;

  /** Prevents instantiation of this static sprite helper. */
  private GameSpriteSheets() {}

  /**
   * Selects the cheese from the processed goal sheet.
   *
   * @param goals loaded processed goal texture
   * @return cheese sprite region
   */
  public static TextureRegion cheese(Texture goals) {
    return frame(goals, 0, 0);
  }

  /**
   * Selects the acorn from the processed goal sheet.
   *
   * @param goals loaded processed goal texture
   * @return acorn sprite region
   */
  public static TextureRegion acorn(Texture goals) {
    return frame(goals, 4, 0);
  }

  /**
   * Selects the right-facing classic mouse used by Random behavior.
   *
   * @param classicMouse loaded processed classic-mouse texture
   * @return classic mouse sprite region
   */
  public static TextureRegion randomSolver(Texture classicMouse) {
    return frame(classicMouse, RIGHT_FACING_COLUMN, 0);
  }

  /**
   * Selects the right-facing basic squirrel used by Scout behavior.
   *
   * @param basicCharacters loaded processed basic-character texture
   * @return basic squirrel sprite region
   */
  public static TextureRegion scoutSquirrel(Texture basicCharacters) {
    return frame(basicCharacters, RIGHT_FACING_COLUMN, SQUIRREL_ROW);
  }

  private static TextureRegion frame(Texture sheet, int column, int row) {
    return new TextureRegion(sheet, column * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
  }
}
