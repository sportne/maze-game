package io.github.sportne.mazegame.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.maze.CellContent;
import io.github.sportne.mazegame.model.maze.MazeState;
import io.github.sportne.mazegame.model.result.BestResult;
import io.github.sportne.mazegame.model.solver.SolverRunResult;
import io.github.sportne.mazegame.state.CellPaletteState;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.LevelProgress;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Draws Maze Game screens using libGDX primitives. */
public final class MazeGameRenderer {
  /** Fill color for simple rectangle buttons. */
  private static final Color BUTTON = new Color(0.18F, 0.20F, 0.24F, 1.0F);

  /** Border color for simple rectangle buttons. */
  private static final Color BUTTON_BORDER = new Color(0.70F, 0.76F, 0.84F, 1.0F);

  /** Muted fill for unavailable level cards. */
  private static final Color LOCKED_BUTTON = new Color(0.10F, 0.11F, 0.13F, 1.0F);

  /** Fill used for the selected palette item. */
  private static final Color SELECTED_BUTTON = new Color(0.20F, 0.32F, 0.44F, 1.0F);

  /** Fill color for empty walkable cells. */
  private static final Color CELL_OPEN = new Color(Color.BLACK);

  /** Temporary fill color for rejected wall placements. */
  private static final Color CELL_REJECTED = new Color(0.95F, 0.42F, 0.42F, 1.0F);

  /** Fill color for the solver start cell before the solver sprite is active. */
  private static final Color CELL_START = new Color(0.24F, 0.62F, 0.95F, 1.0F);

  /** Fill color for player-placed wall cells. */
  private static final Color CELL_WALL = new Color(Color.WHITE);

  /** Amber fill for the walkable Slow Floor cell. */
  private static final Color CELL_SLOW_FLOOR = new Color(0.62F, 0.36F, 0.08F, 1.0F);

  /** High-contrast non-color mark for Slow Floor cells and palette icons. */
  private static final Color SLOW_FLOOR_MARK = new Color(1.0F, 0.88F, 0.54F, 1.0F);

  /** High-contrast non-color mark drawn over a rejected destination. */
  private static final Color REJECTED_MARK = new Color(Color.BLACK);

  /** Preview border for a destination that would accept the dragged type. */
  private static final Color DRAG_VALID = new Color(0.45F, 0.90F, 0.55F, 1.0F);

  /** Preview border for a destination that would reject the dragged type. */
  private static final Color DRAG_REJECTED = new Color(0.95F, 0.42F, 0.42F, 1.0F);

  /** High-contrast corner marks reserving a placed-cell drag source. */
  private static final Color DRAG_SOURCE_RESERVED = new Color(0.40F, 0.82F, 1.0F, 1.0F);

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

  /** Cropped acorn sprite drawn as Scout's endpoint goal. */
  private final TextureRegion acornSprite;

  /** Cropped solver sprite drawn at the current solver position. */
  private final TextureRegion solverSprite;

  /** Distinct Scout sprite drawn for left-priority levels. */
  private final TextureRegion scoutSprite;

  /**
   * Creates a renderer around libGDX drawing resources.
   *
   * @param spriteBatch sprite batch for text and sprites
   * @param shapeRenderer primitive renderer
   * @param font bitmap font
   * @param cheeseSprite cheese sprite region
   * @param acornSprite acorn sprite region
   * @param solverSprite solver sprite region
   * @param scoutSprite Scout sprite region
   */
  public MazeGameRenderer(
      SpriteBatch spriteBatch,
      ShapeRenderer shapeRenderer,
      BitmapFont font,
      TextureRegion cheeseSprite,
      TextureRegion acornSprite,
      TextureRegion solverSprite,
      TextureRegion scoutSprite) {
    this.spriteBatch = Objects.requireNonNull(spriteBatch, "spriteBatch");
    this.shapeRenderer = Objects.requireNonNull(shapeRenderer, "shapeRenderer");
    this.font = Objects.requireNonNull(font, "font");
    this.cheeseSprite = new TextureRegion(Objects.requireNonNull(cheeseSprite, "cheeseSprite"));
    this.acornSprite = new TextureRegion(Objects.requireNonNull(acornSprite, "acornSprite"));
    this.solverSprite = new TextureRegion(Objects.requireNonNull(solverSprite, "solverSprite"));
    this.scoutSprite = new TextureRegion(Objects.requireNonNull(scoutSprite, "scoutSprite"));
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
          case SLOW_FLOOR -> CELL_SLOW_FLOOR;
          case SOLVER_START -> CELL_START;
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
      drawLevelSelect(layout, snapshot.levelProgress());
      return;
    }
    if (snapshot.phase() == GamePhase.SETTINGS) {
      drawSettings(layout, snapshot.audioEnabled());
      return;
    }
    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);
    drawGrid(grid, snapshot);
    drawSlowFloorMarks(grid, snapshot);
    drawCellSprites(grid, snapshot);
    drawRejectedMark(grid, snapshot);
    drawSolver(grid, snapshot);
    drawPaletteDragPreview(layout, grid, snapshot);
    drawControls(layout, snapshot);
    drawGameplayText(layout, snapshot);
    drawPaletteTooltip(layout, snapshot);
  }

  private void drawMainMenu(ScreenLayout layout) {
    ScreenRectangle startButton = layout.bounds(MazeGameLayout.MAIN_MENU_START);
    ScreenRectangle settingsButton = layout.bounds(MazeGameLayout.MAIN_MENU_SETTINGS);
    boolean quitAvailable = layout.element(MazeGameLayout.MAIN_MENU_QUIT).isPresent();
    drawButton(startButton);
    drawButton(settingsButton);
    if (quitAvailable) {
      drawButton(layout.bounds(MazeGameLayout.MAIN_MENU_QUIT));
    }

    spriteBatch.begin();
    font.setColor(TEXT);
    drawTextInRegion(TITLE, layout.bounds(MazeGameLayout.MAIN_MENU_TITLE), 94.0F);
    drawTextInRegion("Start", startButton, 90.0F);
    drawTextInRegion("Settings", settingsButton, 78.0F);
    if (quitAvailable) {
      drawTextInRegion("Quit", layout.bounds(MazeGameLayout.MAIN_MENU_QUIT), 94.0F);
    }
    spriteBatch.end();
  }

  private void drawLevelSelect(ScreenLayout layout, List<LevelProgress> levelProgress) {
    for (int index = 0; index < levelProgress.size(); index++) {
      drawLevelCard(
          layout.bounds(MazeGameLayout.levelCardId(index + 1)),
          levelProgress.get(index).unlocked());
    }
    ScreenRectangle backButton = layout.bounds(MazeGameLayout.LEVEL_SELECT_BACK);
    drawButton(backButton);

    spriteBatch.begin();
    font.setColor(TEXT);
    drawTextInRegion("Select Level", layout.bounds(MazeGameLayout.LEVEL_SELECT_TITLE), 72.0F);
    for (int index = 0; index < levelProgress.size(); index++) {
      LevelProgress progress = levelProgress.get(index);
      ScreenRectangle levelButton = layout.bounds(MazeGameLayout.levelCardId(index + 1));
      font.setColor(progress.unlocked() ? TEXT : PANEL_TEXT);
      drawLevelCardText(progress, levelButton);
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

  private void drawSolver(ScreenRectangle grid, GameRenderSnapshot snapshot) {
    if (snapshot.solverRunResults().isEmpty()) {
      if (snapshot.levelDefinition().solvers().size() > 1) {
        for (LevelSolver solver : snapshot.levelDefinition().solvers()) {
          drawSpriteInCell(grid, snapshot.levelDefinition(), solver.start(), solverSprite(solver));
        }
      }
      return;
    }
    for (int index = 0; index < snapshot.solverRunResults().size(); index++) {
      drawSpriteInCell(
          grid,
          snapshot.levelDefinition(),
          snapshot.solverRunResults().get(index).position(),
          solverSprite(snapshot.levelDefinition().solvers().get(index)));
    }
  }

  private void drawSlowFloorMarks(ScreenRectangle grid, GameRenderSnapshot snapshot) {
    float cellSize = grid.width() / snapshot.levelDefinition().gridSize().columns();
    float inset = Math.max(5.0F, cellSize * 0.24F);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(SLOW_FLOOR_MARK);
    snapshot
        .mazeState()
        .placedCells()
        .forEach(
            (position, type) -> {
              if (type == PlaceableCellType.SLOW_FLOOR) {
                ScreenRectangle mark =
                    insetCellBounds(grid, snapshot.levelDefinition(), position, inset);
                shapeRenderer.rectLine(mark.x(), mark.y(), mark.right(), mark.top(), 2.0F);
                shapeRenderer.rectLine(mark.x(), mark.top(), mark.right(), mark.y(), 2.0F);
                shapeRenderer.rectLine(mark.x(), mark.top(), mark.right(), mark.top(), 2.0F);
                shapeRenderer.rectLine(mark.x(), mark.y(), mark.right(), mark.y(), 2.0F);
              }
            });
    shapeRenderer.end();
  }

  private void drawRejectedMark(ScreenRectangle grid, GameRenderSnapshot snapshot) {
    GridPosition position = snapshot.rejectedPosition();
    if (position == null
        || snapshot.rejectedFlashRemainingSeconds() <= 0.0F
        || position.row() < 0
        || position.row() >= snapshot.levelDefinition().gridSize().rows()
        || position.column() < 0
        || position.column() >= snapshot.levelDefinition().gridSize().columns()) {
      return;
    }
    float cellSize = grid.width() / snapshot.levelDefinition().gridSize().columns();
    float inset = Math.max(4.0F, cellSize * 0.18F);
    ScreenRectangle mark = insetCellBounds(grid, snapshot.levelDefinition(), position, inset);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(REJECTED_MARK);
    shapeRenderer.rectLine(mark.x(), mark.y(), mark.right(), mark.top(), 4.0F);
    shapeRenderer.rectLine(mark.x(), mark.top(), mark.right(), mark.y(), 4.0F);
    shapeRenderer.rectLine(mark.x(), mark.y(), mark.right(), mark.y(), 3.0F);
    shapeRenderer.rectLine(mark.x(), mark.top(), mark.right(), mark.top(), 3.0F);
    shapeRenderer.rectLine(mark.x(), mark.y(), mark.x(), mark.top(), 3.0F);
    shapeRenderer.rectLine(mark.right(), mark.y(), mark.right(), mark.top(), 3.0F);
    shapeRenderer.end();
  }

  private static ScreenRectangle insetCellBounds(
      ScreenRectangle grid, LevelDefinition levelDefinition, GridPosition position, float inset) {
    float cellSize = grid.width() / levelDefinition.gridSize().columns();
    float left = grid.x() + position.column() * cellSize + inset;
    float bottom =
        grid.y() + (levelDefinition.gridSize().rows() - 1 - position.row()) * cellSize + inset;
    return new ScreenRectangle(left, bottom, cellSize - 2.0F * inset, cellSize - 2.0F * inset);
  }

  private void drawPaletteDragPreview(
      ScreenLayout layout, ScreenRectangle grid, GameRenderSnapshot snapshot) {
    PaletteDragPreview preview = snapshot.paletteDragPreview();
    if (preview == null || snapshot.phase() != GamePhase.BUILDING) {
      return;
    }
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    if (preview.sourcePosition() != null) {
      drawDragSourceReservation(grid, snapshot.levelDefinition(), preview.sourcePosition());
    }
    if (preview.destination() != null) {
      drawDragDestination(grid, snapshot.levelDefinition(), preview);
    }
    drawDragIcon(layout.viewport(), preview);
    shapeRenderer.end();
  }

  private void drawDragSourceReservation(
      ScreenRectangle grid, LevelDefinition levelDefinition, GridPosition source) {
    ScreenRectangle mark = insetCellBounds(grid, levelDefinition, source, 5.0F);
    float horizontal = mark.width() * 0.25F;
    float vertical = mark.height() * 0.25F;
    shapeRenderer.setColor(DRAG_SOURCE_RESERVED);
    shapeRenderer.rectLine(mark.x(), mark.y(), mark.x() + horizontal, mark.y(), 3.0F);
    shapeRenderer.rectLine(mark.x(), mark.y(), mark.x(), mark.y() + vertical, 3.0F);
    shapeRenderer.rectLine(mark.right(), mark.y(), mark.right() - horizontal, mark.y(), 3.0F);
    shapeRenderer.rectLine(mark.right(), mark.y(), mark.right(), mark.y() + vertical, 3.0F);
    shapeRenderer.rectLine(mark.x(), mark.top(), mark.x() + horizontal, mark.top(), 3.0F);
    shapeRenderer.rectLine(mark.x(), mark.top(), mark.x(), mark.top() - vertical, 3.0F);
    shapeRenderer.rectLine(mark.right(), mark.top(), mark.right() - horizontal, mark.top(), 3.0F);
    shapeRenderer.rectLine(mark.right(), mark.top(), mark.right(), mark.top() - vertical, 3.0F);
  }

  private void drawDragDestination(
      ScreenRectangle grid, LevelDefinition levelDefinition, PaletteDragPreview preview) {
    ScreenRectangle mark = insetCellBounds(grid, levelDefinition, preview.destination(), 4.0F);
    shapeRenderer.setColor(preview.validDestination() ? DRAG_VALID : DRAG_REJECTED);
    shapeRenderer.rectLine(mark.x(), mark.y(), mark.right(), mark.y(), 4.0F);
    shapeRenderer.rectLine(mark.x(), mark.top(), mark.right(), mark.top(), 4.0F);
    shapeRenderer.rectLine(mark.x(), mark.y(), mark.x(), mark.top(), 4.0F);
    shapeRenderer.rectLine(mark.right(), mark.y(), mark.right(), mark.top(), 4.0F);
    if (preview.validDestination()) {
      shapeRenderer.rectLine(
          mark.x() + mark.width() * 0.20F,
          mark.y() + mark.height() * 0.48F,
          mark.x() + mark.width() * 0.43F,
          mark.y() + mark.height() * 0.24F,
          4.0F);
      shapeRenderer.rectLine(
          mark.x() + mark.width() * 0.43F,
          mark.y() + mark.height() * 0.24F,
          mark.x() + mark.width() * 0.80F,
          mark.y() + mark.height() * 0.76F,
          4.0F);
    } else {
      shapeRenderer.rectLine(mark.x(), mark.y(), mark.right(), mark.top(), 4.0F);
      shapeRenderer.rectLine(mark.x(), mark.top(), mark.right(), mark.y(), 4.0F);
    }
  }

  private void drawDragIcon(ScreenRectangle viewportBounds, PaletteDragPreview preview) {
    ScreenRectangle icon = dragIconBounds(viewportBounds, preview);
    shapeRenderer.setColor(preview.type() == PlaceableCellType.WALL ? CELL_WALL : CELL_SLOW_FLOOR);
    shapeRenderer.rect(icon.x(), icon.y(), icon.width(), icon.height());
    shapeRenderer.setColor(preview.type() == PlaceableCellType.WALL ? GRID_LINE : SLOW_FLOOR_MARK);
    shapeRenderer.rectLine(icon.x(), icon.y(), icon.right(), icon.y(), 3.0F);
    shapeRenderer.rectLine(icon.x(), icon.top(), icon.right(), icon.top(), 3.0F);
    shapeRenderer.rectLine(icon.x(), icon.y(), icon.x(), icon.top(), 3.0F);
    shapeRenderer.rectLine(icon.right(), icon.y(), icon.right(), icon.top(), 3.0F);
    if (preview.type() == PlaceableCellType.SLOW_FLOOR) {
      shapeRenderer.rectLine(icon.x(), icon.y(), icon.right(), icon.top(), 3.0F);
      shapeRenderer.rectLine(icon.x(), icon.top(), icon.right(), icon.y(), 3.0F);
    }
  }

  static ScreenRectangle dragIconBounds(
      ScreenRectangle viewportBounds, PaletteDragPreview preview) {
    float size = 28.0F;
    float left =
        clamp(preview.pointerX() - size / 2.0F, viewportBounds.x(), viewportBounds.right() - size);
    float proposedBottom = preview.pointerY() + 24.0F;
    if (proposedBottom + size > viewportBounds.top()) {
      proposedBottom = preview.pointerY() - size - 24.0F;
    }
    float bottom = clamp(proposedBottom, viewportBounds.y(), viewportBounds.top() - size);
    return new ScreenRectangle(left, bottom, size, size);
  }

  private static float clamp(float value, float minimum, float maximum) {
    return Math.max(minimum, Math.min(value, maximum));
  }

  private TextureRegion solverSprite(LevelSolver solver) {
    return switch (solver.behavior()) {
      case RANDOM -> solverSprite;
      case LEFT_PRIORITY -> scoutSprite;
    };
  }

  private void drawCellSprites(ScreenRectangle grid, GameRenderSnapshot snapshot) {
    LevelDefinition levelDefinition = snapshot.levelDefinition();
    for (LevelSolver solver : levelDefinition.solvers()) {
      TextureRegion goalSprite =
          switch (solver.behavior()) {
            case RANDOM -> cheeseSprite;
            case LEFT_PRIORITY -> acornSprite;
          };
      drawSpriteInCell(grid, levelDefinition, solver.goal(), goalSprite);
    }
  }

  private void drawSpriteInCell(
      ScreenRectangle grid,
      LevelDefinition levelDefinition,
      GridPosition position,
      TextureRegion spriteRegion) {
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

  private void drawControls(ScreenLayout layout, GameRenderSnapshot snapshot) {
    if (snapshot.phase() == GamePhase.BUILDING) {
      drawButton(layout.bounds(MazeGameLayout.BUILD_BACK));
      drawButton(layout.bounds(MazeGameLayout.BUILD_START));
      for (CellPaletteState paletteItem : snapshot.paletteState()) {
        drawPaletteItem(
            layout.bounds(MazeGameLayout.paletteItemId(paletteItem.type())), paletteItem);
      }
    } else if (snapshot.phase() == GamePhase.RESULT) {
      drawButton(layout.bounds(MazeGameLayout.RESULT_RETRY));
      drawButton(layout.bounds(MazeGameLayout.RESULT_REPLAY));
      drawButton(layout.bounds(MazeGameLayout.RESULT_MAIN_MENU));
      if (snapshot.hasNextLevel()) {
        drawButton(layout.bounds(MazeGameLayout.RESULT_NEXT_LEVEL));
      }
    }
  }

  private void drawButton(ScreenRectangle bounds) {
    drawButton(bounds, BUTTON);
  }

  private void drawLevelCard(ScreenRectangle bounds, boolean unlocked) {
    drawButton(bounds, unlocked ? BUTTON : LOCKED_BUTTON);
  }

  private void drawPaletteItem(ScreenRectangle bounds, CellPaletteState state) {
    Color fill = state.selected() ? SELECTED_BUTTON : state.available() ? BUTTON : LOCKED_BUTTON;
    drawButton(bounds, fill, state.selected() ? 4.0F : 2.0F);
    ScreenRectangle icon = paletteIconBounds(bounds);
    ScreenRectangle supplyBadge = paletteSupplyBadgeBounds(icon);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    if (state.type() == PlaceableCellType.WALL) {
      shapeRenderer.setColor(CELL_WALL);
      shapeRenderer.rect(icon.x(), icon.y(), icon.width(), icon.height());
      shapeRenderer.setColor(GRID_LINE);
      shapeRenderer.rectLine(icon.x(), icon.y(), icon.right(), icon.y(), 2.0F);
      shapeRenderer.rectLine(icon.x(), icon.top(), icon.right(), icon.top(), 2.0F);
      shapeRenderer.rectLine(icon.x(), icon.y(), icon.x(), icon.top(), 2.0F);
      shapeRenderer.rectLine(icon.right(), icon.y(), icon.right(), icon.top(), 2.0F);
    } else {
      shapeRenderer.setColor(CELL_SLOW_FLOOR);
      shapeRenderer.rect(icon.x(), icon.y(), icon.width(), icon.height());
      shapeRenderer.setColor(SLOW_FLOOR_MARK);
      shapeRenderer.rectLine(icon.x(), icon.y(), icon.right(), icon.top(), 2.0F);
      shapeRenderer.rectLine(icon.x(), icon.top(), icon.right(), icon.y(), 2.0F);
      shapeRenderer.rectLine(icon.x(), icon.y(), icon.right(), icon.y(), 2.0F);
      shapeRenderer.rectLine(icon.x(), icon.top(), icon.right(), icon.top(), 2.0F);
    }
    drawPaletteSupplyBadgeShape(supplyBadge, state);
    if (!state.available()) {
      shapeRenderer.setColor(TEXT);
      shapeRenderer.rectLine(
          icon.x() - 2.0F, icon.top() + 2.0F, icon.right() + 2.0F, icon.y() - 2.0F, 3.0F);
    }
    shapeRenderer.end();
    String badgeLabel = paletteSupplyBadgeLabel(state);
    if (!badgeLabel.isEmpty()) {
      spriteBatch.begin();
      font.setColor(TEXT);
      drawCenteredText(badgeLabel, supplyBadge);
      spriteBatch.end();
    }
  }

  private void drawPaletteSupplyBadgeShape(ScreenRectangle badge, CellPaletteState state) {
    shapeRenderer.setColor(GRID_LINE);
    shapeRenderer.rect(badge.x(), badge.y(), badge.width(), badge.height());
    shapeRenderer.setColor(TEXT);
    shapeRenderer.rectLine(badge.x(), badge.y(), badge.right(), badge.y(), 1.5F);
    shapeRenderer.rectLine(badge.x(), badge.top(), badge.right(), badge.top(), 1.5F);
    shapeRenderer.rectLine(badge.x(), badge.y(), badge.x(), badge.top(), 1.5F);
    shapeRenderer.rectLine(badge.right(), badge.y(), badge.right(), badge.top(), 1.5F);
    if (state.remainingSupply().isInfinite()) {
      drawInfinityMark(badge);
    }
  }

  private void drawInfinityMark(ScreenRectangle bounds) {
    float centerX = bounds.x() + bounds.width() / 2.0F;
    float centerY = bounds.y() + bounds.height() / 2.0F;
    float left = centerX - 6.0F;
    float right = centerX + 6.0F;
    float top = centerY + 3.5F;
    float bottom = centerY - 3.5F;
    shapeRenderer.rectLine(left, bottom, centerX, top, 1.5F);
    shapeRenderer.rectLine(centerX, top, right, bottom, 1.5F);
    shapeRenderer.rectLine(left, top, centerX, bottom, 1.5F);
    shapeRenderer.rectLine(centerX, bottom, right, top, 1.5F);
  }

  private void drawButton(ScreenRectangle bounds, Color fill) {
    drawButton(bounds, fill, 2.0F);
  }

  private void drawButton(ScreenRectangle bounds, Color fill, float borderWidth) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(fill);
    shapeRenderer.rect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    shapeRenderer.setColor(BUTTON_BORDER);
    shapeRenderer.rectLine(bounds.x(), bounds.y(), bounds.right(), bounds.y(), borderWidth);
    shapeRenderer.rectLine(bounds.x(), bounds.top(), bounds.right(), bounds.top(), borderWidth);
    shapeRenderer.rectLine(bounds.x(), bounds.y(), bounds.x(), bounds.top(), borderWidth);
    shapeRenderer.rectLine(bounds.right(), bounds.y(), bounds.right(), bounds.top(), borderWidth);
    shapeRenderer.end();
  }

  private void drawGameplayText(ScreenLayout layout, GameRenderSnapshot snapshot) {
    spriteBatch.begin();
    if (snapshot.phase() == GamePhase.BUILDING) {
      drawBuildText(layout, snapshot);
    } else if (snapshot.phase() == GamePhase.SOLVER_RUNNING
        || snapshot.phase() == GamePhase.REPLAY) {
      drawRunningText(layout, snapshot);
    } else if (snapshot.phase() == GamePhase.RESULT) {
      drawResultText(layout, snapshot);
    }
    font.setColor(TEXT);
    spriteBatch.end();
  }

  private void drawBuildText(ScreenLayout layout, GameRenderSnapshot snapshot) {
    SolverPresentation presentation =
        SolverPresentation.forBehavior(snapshot.levelDefinition().solverBehavior());
    font.setColor(TEXT);
    drawTextInRegion(
        levelTitle(snapshot.levelDefinition(), presentation, false, Float.MAX_VALUE),
        layout.bounds(MazeGameLayout.BUILD_TITLE),
        0.0F);
    font.draw(
        spriteBatch,
        "Build: " + String.format(Locale.ROOT, "%.1fs", snapshot.buildTimeRemainingSeconds()),
        layout.bounds(MazeGameLayout.BUILD_STATUS).x(),
        textBaseline(layout.bounds(MazeGameLayout.BUILD_STATUS)));
    font.setColor(PANEL_TEXT);
    font.draw(
        spriteBatch,
        buildInstructions(snapshot),
        layout.bounds(MazeGameLayout.BUILD_INSTRUCTIONS).x(),
        textBaseline(layout.bounds(MazeGameLayout.BUILD_INSTRUCTIONS)));
    font.setColor(TEXT);
    drawCenteredText("Back", layout.bounds(MazeGameLayout.BUILD_BACK));
    drawCenteredText(
        snapshot.levelDefinition().solvers().size() > 1 ? "Start Solvers" : "Start Solver",
        layout.bounds(MazeGameLayout.BUILD_START));
  }

  private void drawPaletteTooltip(ScreenLayout layout, GameRenderSnapshot snapshot) {
    PlaceableCellType type = snapshot.paletteTooltipType();
    if (snapshot.phase() != GamePhase.BUILDING || type == null) {
      return;
    }
    CellPaletteState state =
        snapshot.paletteState().stream()
            .filter(item -> item.type() == type)
            .findFirst()
            .orElse(null);
    if (state == null) {
      return;
    }
    ScreenRectangle tooltip =
        paletteTooltipBounds(layout.viewport(), layout.bounds(MazeGameLayout.paletteItemId(type)));
    drawButton(tooltip, BUTTON, 2.0F);
    spriteBatch.begin();
    font.setColor(TEXT);
    drawCenteredText(paletteLabel(state), tooltip);
    spriteBatch.end();
  }

  static ScreenRectangle paletteTooltipBounds(
      ScreenRectangle viewport, ScreenRectangle paletteItem) {
    float margin = 8.0F;
    float width = Math.min(176.0F, viewport.width() - 2.0F * margin);
    float height = 36.0F;
    float x =
        Math.max(
            viewport.x() + margin,
            Math.min(
                paletteItem.x() + (paletteItem.width() - width) / 2.0F,
                viewport.right() - width - margin));
    float above = paletteItem.top() + margin;
    float y = above + height <= viewport.top() - margin ? above : paletteItem.y() - height - margin;
    return new ScreenRectangle(x, y, width, height);
  }

  static String buildInstructions(GameRenderSnapshot snapshot) {
    String target =
        formatSeconds(snapshot.levelDefinition().targetSolveTime().toMillis() / 1000.0F);
    if (snapshot.levelDefinition().supplyFor(PlaceableCellType.SLOW_FLOOR).available()) {
      if (snapshot.levelDefinition().solvers().size() == 1) {
        return String.format(Locale.ROOT, "Tap or drag tools; delay past %s; keep a path", target);
      }
      return String.format(
          Locale.ROOT, "Tap or drag tools; delay both past %s; keep paths", target);
    }
    String goalName =
        SolverPresentation.forBehavior(snapshot.levelDefinition().solverBehavior()).goalName();
    return String.format(Locale.ROOT, "Delay past %s; keep a path to the %s", target, goalName);
  }

  private void drawRunningText(ScreenLayout layout, GameRenderSnapshot snapshot) {
    ScreenRectangle status = layout.bounds(MazeGameLayout.RUN_STATUS);
    String levelTitle =
        levelTitle(
            snapshot.levelDefinition(),
            SolverPresentation.forBehavior(snapshot.levelDefinition().solverBehavior()),
            true,
            status.width());
    font.setColor(TEXT);
    font.draw(
        spriteBatch,
        levelTitle
            + " | "
            + formatSeconds(runTimeRemaining(snapshot))
            + " | >"
            + targetText(snapshot),
        status.x(),
        textBaseline(status));
  }

  private void drawResultText(ScreenLayout layout, GameRenderSnapshot snapshot) {
    ScreenRectangle status = layout.bounds(MazeGameLayout.RESULT_STATUS);
    String levelTitle =
        levelTitle(
            snapshot.levelDefinition(),
            SolverPresentation.forBehavior(snapshot.levelDefinition().solverBehavior()),
            true,
            status.width());
    String outcome = snapshot.resultPassed() ? " | Success | >" : " | Failed | >";
    font.setColor(TEXT);
    font.draw(
        spriteBatch, levelTitle + outcome + targetText(snapshot), status.x(), textBaseline(status));
    font.draw(
        spriteBatch,
        resultStats(snapshot),
        layout.bounds(MazeGameLayout.RESULT_STATS).x(),
        textBaseline(layout.bounds(MazeGameLayout.RESULT_STATS)));
    font.setColor(PANEL_TEXT);
    font.draw(
        spriteBatch,
        "Best: " + bestResultValueText(snapshot.bestResult()),
        layout.bounds(MazeGameLayout.RESULT_BEST).x(),
        textBaseline(layout.bounds(MazeGameLayout.RESULT_BEST)));
    font.setColor(TEXT);
    drawResultAction(layout, MazeGameLayout.RESULT_RETRY, "Retry", "Retry");
    drawResultAction(layout, MazeGameLayout.RESULT_REPLAY, "Replay", "Replay");
    drawResultAction(layout, MazeGameLayout.RESULT_MAIN_MENU, "Main Menu", "Menu");
    if (snapshot.hasNextLevel()) {
      drawResultAction(layout, MazeGameLayout.RESULT_NEXT_LEVEL, "Next Level", "Next");
    } else {
      font.setColor(PANEL_TEXT);
      font.draw(
          spriteBatch,
          noNextLevelText(snapshot),
          layout.bounds(MazeGameLayout.RESULT_NO_NEXT_LEVEL).x(),
          textBaseline(layout.bounds(MazeGameLayout.RESULT_NO_NEXT_LEVEL)));
    }
  }

  private void drawResultAction(
      ScreenLayout layout, String elementId, String fullLabel, String compactLabel) {
    ScreenRectangle bounds = layout.bounds(elementId);
    drawCenteredText(resultActionLabel(bounds.width(), fullLabel, compactLabel), bounds);
  }

  static String resultActionLabel(float width, String fullLabel, String compactLabel) {
    return width < 110.0F ? compactLabel : fullLabel;
  }

  static String paletteLabel(CellPaletteState state) {
    String name = state.type() == PlaceableCellType.WALL ? "Wall" : "Slow";
    String count =
        state.remainingSupply().isInfinite()
            ? "inf"
            : Integer.toString(state.remainingSupply().finiteCount().orElseThrow());
    String selection = state.selected() ? "* " : "";
    String exhausted = state.available() ? "" : " OUT";
    return selection + name + " " + count + exhausted;
  }

  static String paletteSupplyBadgeLabel(CellPaletteState state) {
    return state.remainingSupply().isInfinite()
        ? ""
        : Integer.toString(state.remainingSupply().finiteCount().orElseThrow());
  }

  private static ScreenRectangle paletteIconBounds(ScreenRectangle bounds) {
    float size = Math.min(24.0F, bounds.height() - 16.0F);
    return new ScreenRectangle(
        bounds.x() + (bounds.width() - size) / 2.0F,
        bounds.y() + (bounds.height() - size) / 2.0F,
        size,
        size);
  }

  static ScreenRectangle paletteSupplyBadgeBounds(ScreenRectangle icon) {
    float size = 18.0F;
    return new ScreenRectangle(icon.right() - 9.0F, icon.y() - 7.0F, size, size);
  }

  private static String noNextLevelText(GameRenderSnapshot snapshot) {
    List<LevelProgress> progress = snapshot.levelProgress();
    for (int index = 0; index < progress.size(); index++) {
      if (progress.get(index).levelDefinition().id().equals(snapshot.levelDefinition().id())) {
        return index == progress.size() - 1
            ? "Final available level"
            : "Pass this level to unlock the next";
      }
    }
    return "No next level available";
  }

  private void drawTextInRegion(String text, ScreenRectangle region, float xOffset) {
    font.draw(spriteBatch, text, region.x() + xOffset, textBaseline(region));
  }

  private void drawCenteredText(String text, ScreenRectangle region) {
    drawCenteredText(text, region, textBaseline(region));
  }

  private void drawCenteredText(String text, ScreenRectangle region, float baseline) {
    font.draw(
        spriteBatch,
        text,
        region.x(),
        baseline,
        0,
        text.length(),
        region.width(),
        Align.center,
        false,
        "…");
  }

  private static String levelSelectBestText(BestResult bestResult) {
    return "Best: " + bestResultValueText(bestResult);
  }

  private static String levelSubtitle(LevelProgress progress, float width) {
    if (!progress.unlocked()) {
      return "Locked";
    }
    if (progress.bestResult() != null && width < 200.0F) {
      return String.format(
          Locale.ROOT,
          "Best %.1fs / %d",
          progress.bestResult().elapsedTime().toMillis() / 1000.0F,
          progress.bestResult().moveCount());
    }
    return levelSelectBestText(progress.bestResult());
  }

  private void drawLevelCardText(LevelProgress progress, ScreenRectangle card) {
    drawCenteredText(progress.levelDefinition().name(), card, card.y() + 56.0F);
    drawCenteredText(levelSubtitle(progress, card.width()), card, card.y() + 32.0F);
  }

  private static String targetText(GameRenderSnapshot snapshot) {
    return formatSeconds(snapshot.levelDefinition().targetSolveTime().toMillis() / 1000.0F);
  }

  private static String bestResultValueText(BestResult bestResult) {
    if (bestResult == null) {
      return "--";
    }
    return String.format(
        Locale.ROOT,
        "%.2fs  Moves: %d",
        bestResult.elapsedTime().toMillis() / 1000.0F,
        bestResult.moveCount());
  }

  private static float runTimeRemaining(GameRenderSnapshot snapshot) {
    Duration elapsed =
        snapshot.solverRunResults().stream()
            .map(SolverRunResult::elapsedTime)
            .max(Duration::compareTo)
            .orElse(Duration.ZERO);
    long remainingMillis =
        Math.max(0L, snapshot.levelDefinition().maximumSolveTime().minus(elapsed).toMillis());
    return remainingMillis / 1000.0F;
  }

  private static String levelTitle(
      LevelDefinition level,
      SolverPresentation presentation,
      boolean compact,
      float availableWidth) {
    if (level.solvers().size() > 1) {
      return compact && availableWidth < 360.0F
          ? "Solver + Scout"
          : String.format(Locale.ROOT, "%s | Solver + Scout", level.name());
    }
    return compact
        ? presentation.statusTitle(level.name(), availableWidth)
        : presentation.levelTitle(level.name());
  }

  private static String resultStats(GameRenderSnapshot snapshot) {
    if (snapshot.solverRunResults().size() <= 1) {
      return String.format(
          Locale.ROOT,
          "Time: %.2fs  Moves: %d",
          snapshot.solverRunResult().elapsedTime().toMillis() / 1000.0F,
          snapshot.solverRunResult().moveCount());
    }
    SolverRunResult random = snapshot.solverRunResults().get(0);
    SolverRunResult scout = snapshot.solverRunResults().get(1);
    return String.format(
        Locale.ROOT,
        "Solver %.2fs/%d  Scout %.2fs/%d",
        random.elapsedTime().toMillis() / 1000.0F,
        random.moveCount(),
        scout.elapsedTime().toMillis() / 1000.0F,
        scout.moveCount());
  }

  private static String formatSeconds(float seconds) {
    return String.format(Locale.ROOT, "%.1fs", seconds);
  }

  private static float textBaseline(ScreenRectangle region) {
    return region.y() + Math.min(22.0F, region.height());
  }
}
