package io.github.sportne.mazegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.CellContent;
import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.LevelDefinition;
import io.github.sportne.mazegame.model.MazeState;
import java.util.Locale;
import java.util.Objects;

/** Draws Maze Game screens using libGDX primitives. */
public final class MazeGameRenderer {
  /** Fill color for simple rectangle buttons. */
  private static final Color BUTTON = new Color(0.18F, 0.20F, 0.24F, 1.0F);

  /** Border color for simple rectangle buttons. */
  private static final Color BUTTON_BORDER = new Color(0.70F, 0.76F, 0.84F, 1.0F);

  /** Fill color for empty walkable cells. */
  private static final Color CELL_OPEN = new Color(Color.BLACK);

  /** Temporary fill color for rejected wall placements. */
  private static final Color CELL_REJECTED = new Color(0.95F, 0.42F, 0.42F, 1.0F);

  /** Fill color for the mouse start cell before the mouse sprite is active. */
  private static final Color CELL_START = new Color(0.24F, 0.62F, 0.95F, 1.0F);

  /** Fill color for player-placed wall cells. */
  private static final Color CELL_WALL = new Color(Color.WHITE);

  /** Grid line color drawn over cell fills. */
  private static final Color GRID_LINE = new Color(0.28F, 0.31F, 0.36F, 1.0F);

  /** Secondary text color for instructions and non-primary result messages. */
  private static final Color PANEL_TEXT = new Color(0.62F, 0.70F, 0.78F, 1.0F);

  /** Primary text color. */
  private static final Color TEXT = new Color(0.88F, 0.92F, 0.96F, 1.0F);

  /** Fraction of a cell occupied by centered sprites. */
  private static final float CELL_SPRITE_SCALE = 0.90F;

  /** Desktop window title and in-game title text. */
  private static final String TITLE = "Maze Game";

  /** Sprite batch used for text and sprite regions. */
  private final SpriteBatch spriteBatch;

  /** Primitive renderer used for cells, grid lines, and buttons. */
  private final ShapeRenderer shapeRenderer;

  /** Bitmap font used by the simple UI. */
  private final BitmapFont font;

  /** Cropped cheese sprite drawn over the endpoint cell. */
  private final TextureRegion cheeseSprite;

  /** Cropped mouse sprite drawn at the current mouse position. */
  private final TextureRegion mouseSprite;

  /**
   * Creates a renderer around libGDX drawing resources.
   *
   * @param spriteBatch sprite batch for text and sprites
   * @param shapeRenderer primitive renderer
   * @param font bitmap font
   * @param cheeseSprite cheese sprite region
   * @param mouseSprite mouse sprite region
   */
  public MazeGameRenderer(
      SpriteBatch spriteBatch,
      ShapeRenderer shapeRenderer,
      BitmapFont font,
      TextureRegion cheeseSprite,
      TextureRegion mouseSprite) {
    this.spriteBatch = Objects.requireNonNull(spriteBatch, "spriteBatch");
    this.shapeRenderer = Objects.requireNonNull(shapeRenderer, "shapeRenderer");
    this.font = Objects.requireNonNull(font, "font");
    this.cheeseSprite = cheeseSprite == null ? null : new TextureRegion(cheeseSprite);
    this.mouseSprite = mouseSprite == null ? null : new TextureRegion(mouseSprite);
  }

  /**
   * Returns the fill color for a grid cell.
   *
   * @param mazeState maze to inspect
   * @param rejectedPosition rejected cell, or null
   * @param rejectedFlashRemainingSeconds remaining flash time
   * @param position cell to inspect
   * @return fill color used before sprite overlays
   */
  public static Color cellColor(
      MazeState mazeState,
      GridPosition rejectedPosition,
      float rejectedFlashRemainingSeconds,
      GridPosition position) {
    if (position.equals(rejectedPosition) && rejectedFlashRemainingSeconds > 0.0F) {
      return CELL_REJECTED;
    }
    CellContent content = mazeState.cellContentAt(position);
    Color color =
        switch (content) {
          case EMPTY -> CELL_OPEN;
          case NORMAL_WALL -> CELL_WALL;
          case MOUSE_START -> CELL_START;
          case CHEESE -> CELL_OPEN;
        };
    return new Color(color);
  }

  /**
   * Draws one frame.
   *
   * @param layout declared screen layout
   * @param snapshot render snapshot
   */
  public void render(ScreenLayout layout, GameRenderSnapshot snapshot) {
    if (snapshot.phase() == GamePhase.MAIN_MENU) {
      drawMainMenu(layout);
      return;
    }
    if (snapshot.phase() == GamePhase.LEVEL_SELECT) {
      drawLevelSelect(layout);
      return;
    }
    if (snapshot.phase() == GamePhase.SETTINGS) {
      drawSettings(layout, snapshot.audioEnabled());
      return;
    }
    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);
    drawGrid(grid, snapshot);
    drawCellSprites(grid, snapshot.levelDefinition());
    drawMouse(grid, snapshot);
    drawControls(layout, snapshot.phase());
    drawGameplayText(layout, snapshot);
  }

  private void drawMainMenu(ScreenLayout layout) {
    ScreenRectangle startButton = layout.bounds(MazeGameLayout.MAIN_MENU_START);
    ScreenRectangle settingsButton = layout.bounds(MazeGameLayout.MAIN_MENU_SETTINGS);
    ScreenRectangle quitButton = layout.bounds(MazeGameLayout.MAIN_MENU_QUIT);
    drawButton(startButton);
    drawButton(settingsButton);
    drawButton(quitButton);

    spriteBatch.begin();
    font.setColor(TEXT);
    drawTextInRegion(TITLE, layout.bounds(MazeGameLayout.MAIN_MENU_TITLE), 94.0F);
    drawTextInRegion("Start", startButton, 90.0F);
    drawTextInRegion("Settings", settingsButton, 78.0F);
    drawTextInRegion("Quit", quitButton, 94.0F);
    spriteBatch.end();
  }

  private void drawLevelSelect(ScreenLayout layout) {
    for (int index = 0; index < 6; index++) {
      drawButton(layout.bounds(MazeGameLayout.levelCardId(index + 1)));
    }
    ScreenRectangle backButton = layout.bounds(MazeGameLayout.LEVEL_SELECT_BACK);
    drawButton(backButton);

    spriteBatch.begin();
    font.setColor(TEXT);
    drawTextInRegion("Select Level", layout.bounds(MazeGameLayout.LEVEL_SELECT_TITLE), 72.0F);
    for (int index = 0; index < 6; index++) {
      ScreenRectangle levelButton = layout.bounds(MazeGameLayout.levelCardId(index + 1));
      font.setColor(index == 0 ? TEXT : PANEL_TEXT);
      String title = index == 0 ? "Milestone 1" : "Level " + (index + 1);
      String subtitle = index == 0 ? "5x5" : "Locked";
      font.draw(spriteBatch, title, levelButton.x() + 24.0F, levelButton.y() + 56.0F);
      font.draw(spriteBatch, subtitle, levelButton.x() + 24.0F, levelButton.y() + 32.0F);
    }
    font.setColor(TEXT);
    drawTextInRegion("Back", backButton, 52.0F);
    spriteBatch.end();
  }

  private void drawSettings(ScreenLayout layout, boolean audioEnabled) {
    ScreenRectangle audioButton = layout.bounds(MazeGameLayout.SETTINGS_AUDIO);
    ScreenRectangle backButton = layout.bounds(MazeGameLayout.SETTINGS_BACK);
    drawButton(audioButton);
    drawButton(backButton);

    spriteBatch.begin();
    font.setColor(TEXT);
    drawTextInRegion("Settings", layout.bounds(MazeGameLayout.SETTINGS_TITLE), 90.0F);
    drawTextInRegion("Audio: " + (audioEnabled ? "On" : "Off"), audioButton, 62.0F);
    drawTextInRegion("Back", backButton, 52.0F);
    spriteBatch.end();
  }

  private void drawGrid(ScreenRectangle grid, GameRenderSnapshot snapshot) {
    LevelDefinition levelDefinition = snapshot.levelDefinition();
    float cellSize = grid.width() / levelDefinition.gridSize().columns();
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    for (int row = 0; row < levelDefinition.gridSize().rows(); row++) {
      for (int column = 0; column < levelDefinition.gridSize().columns(); column++) {
        GridPosition position = new GridPosition(row, column);
        shapeRenderer.setColor(
            cellColor(
                snapshot.mazeState(),
                snapshot.rejectedPosition(),
                snapshot.rejectedFlashRemainingSeconds(),
                position));
        shapeRenderer.rect(
            grid.x() + column * cellSize,
            grid.y() + (levelDefinition.gridSize().rows() - 1 - row) * cellSize,
            cellSize,
            cellSize);
      }
    }
    shapeRenderer.setColor(GRID_LINE);
    for (int row = 0; row <= levelDefinition.gridSize().rows(); row++) {
      float y = grid.y() + row * cellSize;
      shapeRenderer.rectLine(grid.x(), y, grid.right(), y, 1.0F);
    }
    for (int column = 0; column <= levelDefinition.gridSize().columns(); column++) {
      float x = grid.x() + column * cellSize;
      shapeRenderer.rectLine(x, grid.y(), x, grid.top(), 1.0F);
    }
    shapeRenderer.end();
  }

  private void drawMouse(ScreenRectangle grid, GameRenderSnapshot snapshot) {
    if (snapshot.mouseRunResult() == null) {
      return;
    }
    drawSpriteInCell(
        grid, snapshot.levelDefinition(), snapshot.mouseRunResult().position(), mouseSprite);
  }

  private void drawCellSprites(ScreenRectangle grid, LevelDefinition levelDefinition) {
    drawSpriteInCell(grid, levelDefinition, levelDefinition.cheese(), cheeseSprite);
  }

  private void drawSpriteInCell(
      ScreenRectangle grid,
      LevelDefinition levelDefinition,
      GridPosition position,
      TextureRegion spriteRegion) {
    if (spriteRegion == null) {
      return;
    }
    ScreenRectangle destination =
        spriteDestination(
            grid,
            levelDefinition,
            position,
            spriteRegion.getRegionWidth(),
            spriteRegion.getRegionHeight());
    spriteBatch.begin();
    spriteBatch.draw(
        spriteRegion, destination.x(), destination.y(), destination.width(), destination.height());
    spriteBatch.end();
  }

  static ScreenRectangle spriteDestination(
      ScreenRectangle grid,
      LevelDefinition levelDefinition,
      GridPosition position,
      float regionWidth,
      float regionHeight) {
    if (regionWidth <= 0.0F || regionHeight <= 0.0F) {
      throw new IllegalArgumentException("sprite dimensions must be positive");
    }
    float cellSize = grid.width() / levelDefinition.gridSize().columns();
    float maxSize = cellSize * CELL_SPRITE_SCALE;
    float aspectRatio = regionWidth / regionHeight;
    float width = maxSize;
    float height = maxSize;
    if (aspectRatio > 1.0F) {
      height = maxSize / aspectRatio;
    } else {
      width = maxSize * aspectRatio;
    }
    float cellLeft = grid.x() + position.column() * cellSize;
    float cellBottom =
        grid.y() + (levelDefinition.gridSize().rows() - 1 - position.row()) * cellSize;
    return new ScreenRectangle(
        cellLeft + (cellSize - width) / 2.0F,
        cellBottom + (cellSize - height) / 2.0F,
        width,
        height);
  }

  private void drawControls(ScreenLayout layout, GamePhase phase) {
    if (phase == GamePhase.BUILDING) {
      drawButton(layout.bounds(MazeGameLayout.BUILD_START));
    } else if (phase == GamePhase.RESULT) {
      drawButton(layout.bounds(MazeGameLayout.RESULT_RETRY));
      drawButton(layout.bounds(MazeGameLayout.RESULT_REPLAY));
      drawButton(layout.bounds(MazeGameLayout.RESULT_MAIN_MENU));
    }
  }

  private void drawButton(ScreenRectangle bounds) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(BUTTON);
    shapeRenderer.rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    shapeRenderer.setColor(BUTTON_BORDER);
    shapeRenderer.rectLine(bounds.x(), bounds.y(), bounds.right(), bounds.y(), 2.0F);
    shapeRenderer.rectLine(bounds.x(), bounds.top(), bounds.right(), bounds.top(), 2.0F);
    shapeRenderer.rectLine(bounds.x(), bounds.y(), bounds.x(), bounds.top(), 2.0F);
    shapeRenderer.rectLine(bounds.right(), bounds.y(), bounds.right(), bounds.top(), 2.0F);
    shapeRenderer.end();
  }

  private void drawGameplayText(ScreenLayout layout, GameRenderSnapshot snapshot) {
    spriteBatch.begin();
    if (snapshot.phase() == GamePhase.BUILDING) {
      drawBuildText(layout, snapshot);
    } else if (snapshot.phase() == GamePhase.RESULT) {
      drawResultText(layout, snapshot);
    }
    font.setColor(TEXT);
    spriteBatch.end();
  }

  private void drawBuildText(ScreenLayout layout, GameRenderSnapshot snapshot) {
    font.setColor(TEXT);
    drawTextInRegion("Maze Game", layout.bounds(MazeGameLayout.BUILD_TITLE), 0.0F);
    font.draw(
        spriteBatch,
        "Build: " + String.format(Locale.ROOT, "%.1fs", snapshot.buildTimeRemainingSeconds()),
        layout.bounds(MazeGameLayout.BUILD_STATUS).x(),
        textBaseline(layout.bounds(MazeGameLayout.BUILD_STATUS)));
    font.setColor(PANEL_TEXT);
    font.draw(
        spriteBatch,
        "Left click: wall   Right click: clear",
        layout.bounds(MazeGameLayout.BUILD_INSTRUCTIONS).x(),
        textBaseline(layout.bounds(MazeGameLayout.BUILD_INSTRUCTIONS)));
    font.setColor(TEXT);
    drawTextInRegion("Start Mouse", layout.bounds(MazeGameLayout.BUILD_START), 44.0F);
  }

  private void drawResultText(ScreenLayout layout, GameRenderSnapshot snapshot) {
    font.setColor(TEXT);
    font.draw(
        spriteBatch,
        snapshot.resultPassed() ? "Pass" : "Fail",
        layout.bounds(MazeGameLayout.RESULT_STATUS).x(),
        textBaseline(layout.bounds(MazeGameLayout.RESULT_STATUS)));
    font.draw(
        spriteBatch,
        "Time: "
            + String.format(
                Locale.ROOT, "%.2fs", snapshot.mouseRunResult().elapsedTime().toMillis() / 1000.0F)
            + "  Moves: "
            + snapshot.mouseRunResult().moveCount(),
        layout.bounds(MazeGameLayout.RESULT_STATS).x(),
        textBaseline(layout.bounds(MazeGameLayout.RESULT_STATS)));
    drawTextInRegion("Retry", layout.bounds(MazeGameLayout.RESULT_RETRY), 46.0F);
    drawTextInRegion("Replay", layout.bounds(MazeGameLayout.RESULT_REPLAY), 42.0F);
    drawTextInRegion("Main Menu", layout.bounds(MazeGameLayout.RESULT_MAIN_MENU), 38.0F);
    if (!snapshot.hasNextLevel()) {
      font.setColor(PANEL_TEXT);
      font.draw(
          spriteBatch,
          "No next level in this milestone",
          layout.bounds(MazeGameLayout.RESULT_NO_NEXT_LEVEL).x(),
          textBaseline(layout.bounds(MazeGameLayout.RESULT_NO_NEXT_LEVEL)));
    }
  }

  private void drawTextInRegion(String text, ScreenRectangle region, float xOffset) {
    font.draw(spriteBatch, text, region.x() + xOffset, textBaseline(region));
  }

  private static float textBaseline(ScreenRectangle region) {
    return region.y() + Math.min(22.0F, region.height());
  }
}
