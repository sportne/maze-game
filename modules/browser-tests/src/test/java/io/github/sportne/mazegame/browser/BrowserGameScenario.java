package io.github.sportne.mazegame.browser;

import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.state.GamePhase;
import java.io.IOException;
import java.util.List;

/** Shared interactions and coordinates for release-browser validation. */
final class BrowserGameScenario {
  static final String MILESTONE_ONE_RESULT_KEY = "maze-game.best-result.milestone-1";
  static final String MILESTONE_TWO_RESULT_KEY = "maze-game.best-result.milestone-2";
  static final String MILESTONE_THREE_RESULT_KEY = "maze-game.best-result.milestone-3";
  static final GridPosition EDITED_CELL = new GridPosition(2, 1);
  static final GridPosition LEVEL_ONE_WALL = new GridPosition(1, 2);
  static final List<GridPosition> MILESTONE_TWO_WALLS = List.of(new GridPosition(2, 2));
  static final List<GridPosition> MILESTONE_THREE_WALLS =
      List.of(new GridPosition(0, 3), new GridPosition(3, 1));
  static final List<GridPosition> MILESTONE_FOUR_WALLS =
      List.of(new GridPosition(3, 1), new GridPosition(1, 0));
  static final GridPosition LEVEL_FIVE_WALL = new GridPosition(1, 5);
  static final List<GridPosition> LEVEL_FIVE_SLOW_FLOORS =
      List.of(new GridPosition(1, 3), new GridPosition(2, 3));
  static final GridPosition LEVEL_SIX_WALL = new GridPosition(2, 3);
  static final List<GridPosition> LEVEL_SIX_SLOW_FLOORS =
      List.of(new GridPosition(1, 2), new GridPosition(2, 2), new GridPosition(3, 2));
  static final GridPosition LEVEL_SEVEN_WALL = new GridPosition(1, 2);
  static final List<GridPosition> LEVEL_SEVEN_SLOW_FLOORS =
      List.of(new GridPosition(1, 1), new GridPosition(2, 0), new GridPosition(2, 1));
  static final GridPosition LEVEL_EIGHT_WALL = new GridPosition(2, 6);
  static final List<GridPosition> LEVEL_EIGHT_SLOW_FLOORS =
      List.of(
          new GridPosition(2, 5),
          new GridPosition(2, 4),
          new GridPosition(3, 4),
          new GridPosition(1, 5));
  static final GridPosition LEVEL_NINE_WALL = new GridPosition(7, 1);
  static final GridPosition LEVEL_NINE_ALTERNATING_GATE = new GridPosition(1, 8);
  static final List<GridPosition> LEVEL_NINE_SLOW_FLOORS =
      List.of(
          new GridPosition(7, 0),
          new GridPosition(3, 6),
          new GridPosition(7, 2),
          new GridPosition(8, 1));
  static final List<GridPosition> LEVEL_TEN_WALLS =
      List.of(new GridPosition(9, 3), new GridPosition(4, 0));
  static final List<GridPosition> LEVEL_TEN_SLOW_FLOORS =
      List.of(
          new GridPosition(7, 0),
          new GridPosition(8, 0),
          new GridPosition(7, 1),
          new GridPosition(9, 1),
          new GridPosition(6, 1),
          new GridPosition(8, 4));
  static final GridPosition LEVEL_TEN_PRESET_WALL = new GridPosition(6, 0);
  static final GridPosition LEVEL_TEN_PRESET_MOVE_DESTINATION = new GridPosition(6, 1);

  private BrowserGameScenario() {}

  static void startMilestoneOne(Controls controls) throws IOException {
    controls.clickButton(
        GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);
    controls.waitForButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(1));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(2));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(3));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(4));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(5));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(6));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(7));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(8));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(9));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(10));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(1));
    controls.waitForButton(
        GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_START);
    controls.clickButton(GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_BACK);
    controls.waitForButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(1));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(1));
    controls.waitForButton(
        GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_START);
    controls.placeAndClearWall(Levels.levelOne(), EDITED_CELL);
    controls.placeWalls(Levels.levelOne(), List.of(LEVEL_ONE_WALL));
    controls.clickButton(GamePhase.BUILDING, Levels.levelOne(), false, MazeGameLayout.BUILD_START);
  }

  static void startMilestoneTwo(Controls controls) throws IOException {
    controls.clickButton(
        GamePhase.RESULT, Levels.levelOne(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
    controls.waitForButton(
        GamePhase.BUILDING, Levels.levelTwo(), false, MazeGameLayout.BUILD_START);
    controls.placeWalls(Levels.levelTwo(), MILESTONE_TWO_WALLS);
    controls.clickButton(GamePhase.BUILDING, Levels.levelTwo(), false, MazeGameLayout.BUILD_START);
  }

  static void startMilestoneThree(Controls controls) throws IOException {
    openMilestoneThree(controls);
    startPreparedMilestoneThree(controls);
  }

  static void openMilestoneThree(Controls controls) throws IOException {
    controls.clickButton(
        GamePhase.RESULT, Levels.levelTwo(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
    controls.waitForButton(
        GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
  }

  static void startMilestoneThreeFromMainMenu(Controls controls) throws IOException {
    controls.clickButton(
        GamePhase.MAIN_MENU, Levels.levelOne(), false, MazeGameLayout.MAIN_MENU_START);
    controls.waitForButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(3));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.levelOne(), false, MazeGameLayout.levelCardId(3));
    controls.waitForButton(
        GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
    startPreparedMilestoneThree(controls);
  }

  private static void startPreparedMilestoneThree(Controls controls) throws IOException {
    controls.placeWalls(Levels.levelThree(), MILESTONE_THREE_WALLS);
    controls.clickButton(
        GamePhase.BUILDING, Levels.levelThree(), false, MazeGameLayout.BUILD_START);
  }

  static ScreenPoint buttonCenter(
      int width,
      int height,
      GamePhase phase,
      LevelDefinition level,
      boolean hasNextLevel,
      String elementId) {
    ScreenRectangle bounds = layout(width, height, phase, level, hasNextLevel).bounds(elementId);
    return new ScreenPoint(
        Math.round(bounds.x() + bounds.width() / 2.0F),
        Math.round(height - bounds.y() - bounds.height() / 2.0F));
  }

  static ScreenPoint cellCenter(
      int width, int height, LevelDefinition level, GridPosition position) {
    ScreenRectangle grid =
        layout(width, height, GamePhase.BUILDING, level, false).bounds(MazeGameLayout.GAME_GRID);
    float cellWidth = grid.width() / level.gridSize().columns();
    float cellHeight = grid.height() / level.gridSize().rows();
    float x = grid.x() + (position.column() + 0.5F) * cellWidth;
    float yFromBottom = grid.y() + (level.gridSize().rows() - position.row() - 0.5F) * cellHeight;
    return new ScreenPoint(Math.round(x), Math.round(height - yFromBottom));
  }

  private static ScreenLayout layout(
      int width, int height, GamePhase phase, LevelDefinition level, boolean hasNextLevel) {
    return MazeGameLayout.forPhase(
        phase,
        width,
        height,
        level.gridSize(),
        false,
        Levels.catalog().levels().size(),
        hasNextLevel,
        level.initiallyAvailableCellTypes());
  }

  interface Controls {
    void clickButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId);

    void waitForButton(
        GamePhase phase, LevelDefinition level, boolean hasNextLevel, String elementId)
        throws IOException;

    void placeAndClearWall(LevelDefinition level, GridPosition position) throws IOException;

    void placeWalls(LevelDefinition level, List<GridPosition> walls) throws IOException;
  }

  record ScreenPoint(int x, int y) {}
}
