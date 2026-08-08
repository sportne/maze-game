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
  static final GridPosition EDITED_CELL = new GridPosition(2, 1);
  static final List<GridPosition> MILESTONE_TWO_WALLS =
      List.of(
          new GridPosition(1, 1),
          new GridPosition(1, 4),
          new GridPosition(2, 0),
          new GridPosition(2, 6),
          new GridPosition(3, 3),
          new GridPosition(3, 6),
          new GridPosition(4, 0),
          new GridPosition(5, 0),
          new GridPosition(5, 2));

  private BrowserGameScenario() {}

  static void startMilestoneOne(Controls controls) throws IOException {
    controls.clickButton(
        GamePhase.MAIN_MENU, Levels.milestoneOne(), false, MazeGameLayout.MAIN_MENU_START);
    controls.waitForButton(
        GamePhase.LEVEL_SELECT, Levels.milestoneOne(), false, MazeGameLayout.levelCardId(1));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.milestoneOne(), false, MazeGameLayout.levelCardId(2));
    controls.clickButton(
        GamePhase.LEVEL_SELECT, Levels.milestoneOne(), false, MazeGameLayout.levelCardId(1));
    controls.waitForButton(
        GamePhase.BUILDING, Levels.milestoneOne(), false, MazeGameLayout.BUILD_START);
    controls.placeAndClearWall(Levels.milestoneOne(), EDITED_CELL);
    controls.clickButton(
        GamePhase.BUILDING, Levels.milestoneOne(), false, MazeGameLayout.BUILD_START);
  }

  static void startMilestoneTwo(Controls controls) throws IOException {
    controls.clickButton(
        GamePhase.RESULT, Levels.milestoneOne(), true, MazeGameLayout.RESULT_NEXT_LEVEL);
    controls.waitForButton(
        GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);
    controls.placeWalls(Levels.milestoneTwo(), MILESTONE_TWO_WALLS);
    controls.clickButton(
        GamePhase.BUILDING, Levels.milestoneTwo(), false, MazeGameLayout.BUILD_START);
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
    return MazeGameLayout.forPhase(phase, width, height, level.gridSize(), false, 2, hasNextLevel);
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
