package io.github.sportne.mazegame.assets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Selects game sprites from the normalized runtime sheets produced by the asset pipeline. */
public final class GameSpriteSheets {
  /** Width and height of every normalized runtime frame. */
  private static final int CELL_SIZE = 128;

  /** Number of directional frames stored for each character. */
  private static final int DIRECTION_COUNT = 4;

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
    return randomSolverSprites(classicMouse).defaultSprite();
  }

  /** Returns every directional frame for the classic mouse used by Random behavior. */
  public static DirectionalSpriteSet randomSolverSprites(Texture classicMouse) {
    return directionalFrames(classicMouse, 0);
  }

  /**
   * Selects the right-facing basic squirrel used by Scout behavior.
   *
   * @param basicCharacters loaded processed basic-character texture
   * @return basic squirrel sprite region
   */
  public static TextureRegion scoutSquirrel(Texture basicCharacters) {
    return scoutSquirrelSprites(basicCharacters).defaultSprite();
  }

  /** Returns every directional frame for the basic squirrel used by Scout behavior. */
  public static DirectionalSpriteSet scoutSquirrelSprites(Texture basicCharacters) {
    return directionalFrames(basicCharacters, SQUIRREL_ROW);
  }

  private static DirectionalSpriteSet directionalFrames(Texture sheet, int row) {
    TextureRegion[] frames = new TextureRegion[DIRECTION_COUNT];
    for (int column = 0; column < frames.length; column++) {
      frames[column] = frame(sheet, column, row);
    }
    return new DirectionalSpriteSet(frames[0], frames[1], frames[2], frames[3]);
  }

  private static TextureRegion frame(Texture sheet, int column, int row) {
    return new TextureRegion(sheet, column * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
  }
}
