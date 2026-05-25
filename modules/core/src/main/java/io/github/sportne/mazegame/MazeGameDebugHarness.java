package io.github.sportne.mazegame;

import com.badlogic.gdx.Input;
import io.github.sportne.mazegame.model.GridPosition;
import java.time.Duration;

/** Test and debug harness that simulates desktop clicks against the game layout. */
public final class MazeGameDebugHarness {
  public static final int DEFAULT_SCREEN_WIDTH = 1280;
  public static final int DEFAULT_SCREEN_HEIGHT = 720;

  private final MazeGame game;
  private final int screenWidth;
  private final int screenHeight;

  public MazeGameDebugHarness() {
    this(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);
  }

  public MazeGameDebugHarness(int screenWidth, int screenHeight) {
    if (screenWidth <= 0 || screenHeight <= 0) {
      throw new IllegalArgumentException("screen dimensions must be positive");
    }
    this.game = new MazeGame();
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
  }

  public MazeGameDebugHarness leftClickCell(int row, int column) {
    return leftClickCell(new GridPosition(row, column));
  }

  public MazeGameDebugHarness leftClickCell(GridPosition position) {
    clickCell(position, Input.Buttons.LEFT);
    return this;
  }

  public MazeGameDebugHarness rightClickCell(int row, int column) {
    return rightClickCell(new GridPosition(row, column));
  }

  public MazeGameDebugHarness rightClickCell(GridPosition position) {
    clickCell(position, Input.Buttons.RIGHT);
    return this;
  }

  public MazeGameDebugHarness clickStartRun() {
    clickButton(currentLayout().startButtonBounds(), Input.Buttons.LEFT);
    return this;
  }

  public MazeGameDebugHarness clickRetry() {
    clickButton(MazeGame.retryButtonBounds(currentLayout()), Input.Buttons.LEFT);
    return this;
  }

  public MazeGameDebugHarness clickReplay() {
    clickButton(MazeGame.replayButtonBounds(currentLayout()), Input.Buttons.LEFT);
    return this;
  }

  public MazeGameDebugHarness advance(Duration duration) {
    game.updateGame(duration.toMillis() / 1000.0F);
    return this;
  }

  public MazeGameDebugSnapshot snapshot() {
    return new MazeGameDebugSnapshot(
        game.gamePhase(),
        game.mazeState(),
        game.buildTimeRemainingSeconds(),
        game.rejectedPosition(),
        game.mouseRunResult(),
        game.resultPassed(),
        game.hasNextLevel());
  }

  private void clickCell(GridPosition position, int button) {
    GridBounds gridBounds = currentLayout().gridBounds();
    float x = gridBounds.x() + (position.column() + 0.5F) * gridBounds.cellSize();
    float yFromBottom =
        gridBounds.y()
            + (gridBounds.gridSize().rows() - 1 - position.row() + 0.5F) * gridBounds.cellSize();
    clickAtBottomLeftCoordinates(x, yFromBottom, button);
  }

  private void clickButton(ButtonBounds bounds, int button) {
    clickAtBottomLeftCoordinates(
        bounds.x() + bounds.width() / 2.0F, bounds.y() + bounds.height() / 2.0F, button);
  }

  private void clickAtBottomLeftCoordinates(float x, float y, int button) {
    game.handleScreenClick(
        Math.round(x), Math.round(screenHeight - y), button, screenWidth, screenHeight);
  }

  private BuildPhaseLayout currentLayout() {
    return BuildPhaseLayout.centered(
        screenWidth, screenHeight, game.mazeState().levelDefinition().gridSize());
  }
}
