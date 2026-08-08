package io.github.sportne.mazegame.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.Input;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.Levels;
import io.github.sportne.mazegame.state.GamePhase;
import io.github.sportne.mazegame.state.LevelProgress;
import java.util.List;
import org.junit.jupiter.api.Test;

final class GameInputRouterTest {
  private static final GridSize GRID_SIZE = Levels.milestoneOne().gridSize();
  private static final List<LevelProgress> LEVEL_PROGRESS =
      List.of(
          new LevelProgress(Levels.milestoneOne(), true, null),
          new LevelProgress(Levels.milestoneTwo(), false, null));
  private static final int SCREEN_WIDTH = 1280;
  private static final int SCREEN_HEIGHT = 720;

  @Test
  void routesMainMenuButtons() {
    assertEquals(
        GameInputActionType.OPEN_LEVEL_SELECT,
        click(GamePhase.MAIN_MENU, MazeGameLayout.MAIN_MENU_START).type());
    assertEquals(
        GameInputActionType.OPEN_SETTINGS,
        click(GamePhase.MAIN_MENU, MazeGameLayout.MAIN_MENU_SETTINGS).type());
    assertEquals(
        GameInputActionType.QUIT, click(GamePhase.MAIN_MENU, MazeGameLayout.MAIN_MENU_QUIT).type());
  }

  @Test
  void missingQuitControlIsNotRouted() {
    ScreenLayout layout =
        MazeGameLayout.forPhase(GamePhase.MAIN_MENU, SCREEN_WIDTH, SCREEN_HEIGHT, GRID_SIZE, false);

    GameInputAction action =
        GameInputRouter.route(
            layout,
            GamePhase.MAIN_MENU,
            SCREEN_WIDTH / 2,
            432,
            Input.Buttons.LEFT,
            GRID_SIZE,
            LEVEL_PROGRESS);

    assertEquals(GameInputAction.NONE, action);
  }

  @Test
  void routesLevelSelectButtons() {
    GameInputAction selection = click(GamePhase.LEVEL_SELECT, MazeGameLayout.levelCardId(1));
    assertEquals(GameInputActionType.SELECT_LEVEL, selection.type());
    assertEquals(Levels.milestoneOne().id(), selection.levelId());
    assertEquals(
        GameInputActionType.SELECT_LOCKED_LEVEL,
        click(GamePhase.LEVEL_SELECT, MazeGameLayout.levelCardId(2)).type());
    assertEquals(
        GameInputActionType.BACK_TO_MAIN_MENU,
        click(GamePhase.LEVEL_SELECT, MazeGameLayout.LEVEL_SELECT_BACK).type());
  }

  @Test
  void routesEverySuppliedSelectableLevelByStableId() {
    GameInputAction selection =
        click(
            GamePhase.LEVEL_SELECT,
            MazeGameLayout.levelCardId(2),
            List.of(
                new LevelProgress(Levels.milestoneOne(), true, null),
                new LevelProgress(Levels.milestoneTwo(), true, null)));

    assertEquals(GameInputActionType.SELECT_LEVEL, selection.type());
    assertEquals(Levels.milestoneTwo().id(), selection.levelId());
  }

  @Test
  void routesSettingsButtons() {
    assertEquals(
        GameInputActionType.TOGGLE_AUDIO,
        click(GamePhase.SETTINGS, MazeGameLayout.SETTINGS_AUDIO).type());
    assertEquals(
        GameInputActionType.BACK_TO_MAIN_MENU,
        click(GamePhase.SETTINGS, MazeGameLayout.SETTINGS_BACK).type());
  }

  @Test
  void routesBuildControlsAndGridCells() {
    assertEquals(
        GameInputActionType.TOGGLE_WALL_MODE,
        click(GamePhase.BUILDING, MazeGameLayout.BUILD_WALL_MODE).type());
    assertEquals(
        GameInputActionType.START_RUN,
        click(GamePhase.BUILDING, MazeGameLayout.BUILD_START).type());

    GameInputAction place =
        GameInputRouter.route(
            layout(GamePhase.BUILDING),
            GamePhase.BUILDING,
            640,
            360,
            Input.Buttons.LEFT,
            GRID_SIZE,
            LEVEL_PROGRESS);
    GameInputAction clear =
        GameInputRouter.route(
            layout(GamePhase.BUILDING),
            GamePhase.BUILDING,
            640,
            360,
            Input.Buttons.RIGHT,
            GRID_SIZE,
            LEVEL_PROGRESS);

    assertEquals(GameInputActionType.PLACE_WALL, place.type());
    assertEquals(new GridPosition(2, 2), place.position());
    assertEquals(GameInputActionType.CLEAR_WALL, clear.type());
    assertEquals(new GridPosition(2, 2), clear.position());
  }

  @Test
  void routesGridBoundaryCells() {
    ScreenLayout layout = layout(GamePhase.BUILDING);
    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);

    GameInputAction topLeft =
        GameInputRouter.route(
            layout,
            GamePhase.BUILDING,
            Math.round(grid.x() + 1.0F),
            Math.round(SCREEN_HEIGHT - grid.top() + 1.0F),
            Input.Buttons.LEFT,
            GRID_SIZE,
            LEVEL_PROGRESS);
    GameInputAction bottomRight =
        GameInputRouter.route(
            layout,
            GamePhase.BUILDING,
            Math.round(grid.right() - 1.0F),
            Math.round(SCREEN_HEIGHT - grid.y() - 1.0F),
            Input.Buttons.LEFT,
            GRID_SIZE,
            LEVEL_PROGRESS);

    assertEquals(new GridPosition(0, 0), topLeft.position());
    assertEquals(new GridPosition(4, 4), bottomRight.position());
  }

  @Test
  void ignoresClicksOutsideTheBuildGrid() {
    ScreenLayout layout = layout(GamePhase.BUILDING);
    ScreenRectangle grid = layout.bounds(MazeGameLayout.GAME_GRID);

    GameInputAction action =
        GameInputRouter.route(
            layout,
            GamePhase.BUILDING,
            Math.round(grid.x() - 1.0F),
            Math.round(SCREEN_HEIGHT - grid.top() + 1.0F),
            Input.Buttons.RIGHT,
            GRID_SIZE,
            LEVEL_PROGRESS);

    assertEquals(GameInputAction.NONE, action);
  }

  @Test
  void consumesUnsupportedMouseButtonsInsideTheBuildGrid() {
    GameInputAction action =
        GameInputRouter.route(
            layout(GamePhase.BUILDING), GamePhase.BUILDING, 640, 360, 7, GRID_SIZE, LEVEL_PROGRESS);

    assertEquals(GameInputActionType.IGNORED_GRID_CLICK, action.type());
    assertTrue(action.consumed());
  }

  @Test
  void routesResultButtons() {
    assertEquals(
        GameInputActionType.RETRY, click(GamePhase.RESULT, MazeGameLayout.RESULT_RETRY).type());
    assertEquals(
        GameInputActionType.REPLAY, click(GamePhase.RESULT, MazeGameLayout.RESULT_REPLAY).type());
    assertEquals(
        GameInputActionType.RESULT_MAIN_MENU,
        click(GamePhase.RESULT, MazeGameLayout.RESULT_MAIN_MENU).type());
    assertEquals(
        GameInputActionType.NEXT_LEVEL,
        click(GamePhase.RESULT, MazeGameLayout.RESULT_NEXT_LEVEL, LEVEL_PROGRESS, true).type());
  }

  @Test
  void ignoresInactiveOrMissedClicks() {
    GameInputAction action =
        GameInputRouter.route(
            layout(GamePhase.MOUSE_RUNNING),
            GamePhase.MOUSE_RUNNING,
            640,
            360,
            Input.Buttons.LEFT,
            GRID_SIZE,
            LEVEL_PROGRESS);

    assertEquals(GameInputAction.NONE, action);
    assertFalse(action.consumed());
  }

  @Test
  void cellActionsRequireCellPayloads() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameInputAction(GameInputActionType.PLACE_WALL, null, null));
  }

  @Test
  void nonCellActionsRejectCellPayloads() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameInputAction(GameInputActionType.RETRY, new GridPosition(2, 2), null));
  }

  @Test
  void levelSelectionRequiresOnlyAStableLevelId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameInputAction(GameInputActionType.SELECT_LEVEL, null, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameInputAction(GameInputActionType.SELECT_LEVEL, null, " "));
    assertThrows(
        IllegalArgumentException.class,
        () -> new GameInputAction(GameInputActionType.RETRY, null, Levels.milestoneTwo().id()));

    assertEquals(
        Levels.milestoneTwo().id(),
        GameInputAction.selectLevel(Levels.milestoneTwo().id()).levelId());
  }

  @Test
  void nonNoneActionsAreConsumed() {
    assertTrue(GameInputAction.of(GameInputActionType.RETRY).consumed());
    assertFalse(GameInputAction.NONE.consumed());
  }

  private static GameInputAction click(GamePhase phase, String elementId) {
    return click(phase, elementId, LEVEL_PROGRESS, false);
  }

  private static GameInputAction click(
      GamePhase phase, String elementId, List<LevelProgress> levelProgress) {
    return click(phase, elementId, levelProgress, false);
  }

  private static GameInputAction click(
      GamePhase phase, String elementId, List<LevelProgress> levelProgress, boolean hasNextLevel) {
    ScreenLayout layout = layout(phase, levelProgress.size(), hasNextLevel);
    ScreenRectangle bounds = layout.bounds(elementId);
    return GameInputRouter.route(
        layout,
        phase,
        Math.round(bounds.x() + bounds.width() / 2.0F),
        Math.round(SCREEN_HEIGHT - bounds.y() - bounds.height() / 2.0F),
        Input.Buttons.LEFT,
        GRID_SIZE,
        levelProgress);
  }

  private static ScreenLayout layout(GamePhase phase) {
    return layout(phase, LEVEL_PROGRESS.size(), false);
  }

  private static ScreenLayout layout(GamePhase phase, int levelCount, boolean hasNextLevel) {
    return MazeGameLayout.forPhase(
        phase, SCREEN_WIDTH, SCREEN_HEIGHT, GRID_SIZE, true, levelCount, hasNextLevel);
  }
}
