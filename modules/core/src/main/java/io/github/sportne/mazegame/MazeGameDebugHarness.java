package io.github.sportne.mazegame;

import com.badlogic.gdx.Input;
import io.github.sportne.mazegame.model.GridPosition;
import java.time.Duration;

/**
 * Test and debug harness that simulates desktop clicks against the game layout.
 *
 * <p>The harness drives the same coordinate conversion and click handling used by the desktop
 * window, but it does not create a libGDX window. That makes it useful for fast tests and for
 * scripting interactions when validating maze behavior.
 */
public final class MazeGameDebugHarness {
  /** Default desktop window width used by the LWJGL launcher. */
  public static final int DEFAULT_SCREEN_WIDTH = 1280;

  /** Default desktop window height used by the LWJGL launcher. */
  public static final int DEFAULT_SCREEN_HEIGHT = 720;

  /** Game instance being driven by simulated input. */
  private final MazeGame game;

  /** Virtual screen width used for layout calculations. */
  private final int screenWidth;

  /** Virtual screen height used for layout calculations. */
  private final int screenHeight;

  /** Creates a harness using the default desktop window size. */
  public MazeGameDebugHarness() {
    this(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT, true);
  }

  /**
   * Creates a harness for a specific virtual screen size.
   *
   * @param screenWidth virtual window width in pixels
   * @param screenHeight virtual window height in pixels
   */
  public MazeGameDebugHarness(int screenWidth, int screenHeight) {
    this(screenWidth, screenHeight, true);
  }

  /**
   * Creates a harness that begins on the startup menu.
   *
   * @return harness that can drive menu navigation before gameplay
   */
  public static MazeGameDebugHarness forStartupMenu() {
    return new MazeGameDebugHarness(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT, false);
  }

  /**
   * Creates a harness for a specific virtual screen size.
   *
   * @param screenWidth virtual window width in pixels
   * @param screenHeight virtual window height in pixels
   * @param startInLevel true to jump directly to Milestone 1 for legacy interaction tests
   */
  private MazeGameDebugHarness(int screenWidth, int screenHeight, boolean startInLevel) {
    if (screenWidth <= 0 || screenHeight <= 0) {
      throw new IllegalArgumentException("screen dimensions must be positive");
    }
    this.game = new MazeGame();
    if (startInLevel) {
      game.startMilestoneOneLevel();
    }
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
  }

  /**
   * Simulates a left click on a grid cell.
   *
   * @param row zero-based row from the top of the grid
   * @param column zero-based column from the left of the grid
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness leftClickCell(int row, int column) {
    return leftClickCell(new GridPosition(row, column));
  }

  /**
   * Simulates a left click on a grid cell.
   *
   * @param position grid position to click
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness leftClickCell(GridPosition position) {
    clickCell(position, Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates a right click on a grid cell.
   *
   * @param row zero-based row from the top of the grid
   * @param column zero-based column from the left of the grid
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness rightClickCell(int row, int column) {
    return rightClickCell(new GridPosition(row, column));
  }

  /**
   * Simulates a right click on a grid cell.
   *
   * @param position grid position to click
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness rightClickCell(GridPosition position) {
    clickCell(position, Input.Buttons.RIGHT);
    return this;
  }

  /**
   * Simulates clicking the Start Mouse button.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickStartRun() {
    clickButton(currentLayout().startButtonBounds(), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the startup menu Start button.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickMainMenuStart() {
    clickButton(MazeGame.mainMenuStartButtonBounds(screenWidth, screenHeight), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the startup menu Settings button.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickMainMenuSettings() {
    clickButton(
        MazeGame.mainMenuSettingsButtonBounds(screenWidth, screenHeight), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the enabled Milestone 1 card.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickMilestoneOneLevel() {
    clickButton(MazeGame.levelButtonBounds(screenWidth, screenHeight, 0), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking a locked future level card.
   *
   * @param index zero-based level card index from 1 to 5
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickLockedLevel(int index) {
    clickButton(MazeGame.levelButtonBounds(screenWidth, screenHeight, index), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the settings audio toggle.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickSettingsAudio() {
    clickButton(MazeGame.settingsAudioButtonBounds(screenWidth, screenHeight), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the settings Back button.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickSettingsBack() {
    clickButton(MazeGame.settingsBackButtonBounds(screenWidth, screenHeight), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the result screen Main Menu button.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickResultMainMenu() {
    clickButton(MazeGame.resultMainMenuButtonBounds(currentLayout()), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the Retry button in the result phase.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickRetry() {
    clickButton(MazeGame.retryButtonBounds(currentLayout()), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Simulates clicking the Replay button in the result phase.
   *
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness clickReplay() {
    clickButton(MazeGame.replayButtonBounds(currentLayout()), Input.Buttons.LEFT);
    return this;
  }

  /**
   * Advances game time.
   *
   * @param duration amount of game time to advance
   * @return this harness for fluent scripting
   */
  public MazeGameDebugHarness advance(Duration duration) {
    game.updateGame(duration.toMillis() / 1000.0F);
    return this;
  }

  /**
   * Returns a snapshot of the current game state.
   *
   * @return immutable debug snapshot
   */
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

  /**
   * Converts a grid cell to screen coordinates and dispatches a click.
   *
   * @param position grid cell to click
   * @param button libGDX mouse button code
   */
  private void clickCell(GridPosition position, int button) {
    GridBounds gridBounds = currentLayout().gridBounds();
    float x = gridBounds.x() + (position.column() + 0.5F) * gridBounds.cellSize();
    float yFromBottom =
        gridBounds.y()
            + (gridBounds.gridSize().rows() - 1 - position.row() + 0.5F) * gridBounds.cellSize();
    clickAtBottomLeftCoordinates(x, yFromBottom, button);
  }

  /**
   * Converts a button rectangle to its center point and dispatches a click.
   *
   * @param bounds button bounds to click
   * @param button libGDX mouse button code
   */
  private void clickButton(ButtonBounds bounds, int button) {
    clickAtBottomLeftCoordinates(
        bounds.x() + bounds.width() / 2.0F, bounds.y() + bounds.height() / 2.0F, button);
  }

  /**
   * Dispatches a click after converting bottom-left coordinates to desktop input coordinates.
   *
   * @param x x coordinate from the left edge of the virtual window
   * @param y y coordinate from the bottom edge of the virtual window
   * @param button libGDX mouse button code
   */
  private void clickAtBottomLeftCoordinates(float x, float y, int button) {
    game.handleScreenClick(
        Math.round(x), Math.round(screenHeight - y), button, screenWidth, screenHeight);
  }

  /**
   * Returns the current layout for the harness screen size.
   *
   * @return layout matching the game's current level grid
   */
  private BuildPhaseLayout currentLayout() {
    return BuildPhaseLayout.centered(
        screenWidth, screenHeight, game.mazeState().levelDefinition().gridSize());
  }
}
