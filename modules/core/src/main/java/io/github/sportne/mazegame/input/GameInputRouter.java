package io.github.sportne.mazegame.input;

import com.badlogic.gdx.Input;
import io.github.sportne.mazegame.layout.MazeGameLayout;
import io.github.sportne.mazegame.layout.ScreenLayout;
import io.github.sportne.mazegame.layout.ScreenRectangle;
import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.GridSize;
import java.util.Optional;

/** Converts raw mouse clicks into game input actions. */
public final class GameInputRouter {
  /** Prevents instantiation of this stateless router. */
  private GameInputRouter() {}

  /**
   * Routes one desktop mouse click.
   *
   * @param layout current declared screen layout
   * @param phase current game phase
   * @param screenX x coordinate from the left edge
   * @param screenY y coordinate from the top edge
   * @param button libGDX mouse button code
   * @param gridSize current level grid size
   * @return routed input action
   */
  public static GameInputAction route(
      ScreenLayout layout,
      GamePhase phase,
      int screenX,
      int screenY,
      int button,
      GridSize gridSize) {
    float screenYFromBottom = layout.viewport().height() - screenY;
    if (button == Input.Buttons.LEFT) {
      GameInputAction controlAction = routeLeftClick(layout, phase, screenX, screenYFromBottom);
      if (controlAction.consumed()) {
        return controlAction;
      }
    }
    if (phase == GamePhase.BUILDING) {
      return routeBuildGridClick(layout, screenX, screenYFromBottom, button, gridSize);
    }
    return GameInputAction.NONE;
  }

  private static GameInputAction routeLeftClick(
      ScreenLayout layout, GamePhase phase, int screenX, float screenYFromBottom) {
    return switch (phase) {
      case MAIN_MENU -> routeMainMenu(layout, screenX, screenYFromBottom);
      case LEVEL_SELECT -> routeLevelSelect(layout, screenX, screenYFromBottom);
      case SETTINGS -> routeSettings(layout, screenX, screenYFromBottom);
      case BUILDING ->
          contains(layout, MazeGameLayout.BUILD_START, screenX, screenYFromBottom)
              ? GameInputAction.of(GameInputActionType.START_RUN)
              : GameInputAction.NONE;
      case RESULT -> routeResult(layout, screenX, screenYFromBottom);
      case MOUSE_RUNNING, REPLAY -> GameInputAction.NONE;
    };
  }

  private static GameInputAction routeMainMenu(
      ScreenLayout layout, int screenX, float screenYFromBottom) {
    if (contains(layout, MazeGameLayout.MAIN_MENU_START, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.OPEN_LEVEL_SELECT);
    }
    if (contains(layout, MazeGameLayout.MAIN_MENU_SETTINGS, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.OPEN_SETTINGS);
    }
    if (contains(layout, MazeGameLayout.MAIN_MENU_QUIT, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.QUIT);
    }
    return GameInputAction.NONE;
  }

  private static GameInputAction routeLevelSelect(
      ScreenLayout layout, int screenX, float screenYFromBottom) {
    if (contains(layout, MazeGameLayout.LEVEL_SELECT_BACK, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.BACK_TO_MAIN_MENU);
    }
    for (int index = 0; index < 6; index++) {
      if (contains(layout, MazeGameLayout.levelCardId(index + 1), screenX, screenYFromBottom)) {
        return GameInputAction.of(
            index == 0
                ? GameInputActionType.START_MILESTONE_ONE
                : GameInputActionType.SELECT_LOCKED_LEVEL);
      }
    }
    return GameInputAction.NONE;
  }

  private static GameInputAction routeSettings(
      ScreenLayout layout, int screenX, float screenYFromBottom) {
    if (contains(layout, MazeGameLayout.SETTINGS_BACK, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.BACK_TO_MAIN_MENU);
    }
    if (contains(layout, MazeGameLayout.SETTINGS_AUDIO, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.TOGGLE_AUDIO);
    }
    return GameInputAction.NONE;
  }

  private static GameInputAction routeResult(
      ScreenLayout layout, int screenX, float screenYFromBottom) {
    if (contains(layout, MazeGameLayout.RESULT_RETRY, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.RETRY);
    }
    if (contains(layout, MazeGameLayout.RESULT_REPLAY, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.REPLAY);
    }
    if (contains(layout, MazeGameLayout.RESULT_MAIN_MENU, screenX, screenYFromBottom)) {
      return GameInputAction.of(GameInputActionType.RESULT_MAIN_MENU);
    }
    return GameInputAction.NONE;
  }

  private static GameInputAction routeBuildGridClick(
      ScreenLayout layout, int screenX, float screenYFromBottom, int button, GridSize gridSize) {
    Optional<GridPosition> position =
        gridPositionAt(
            layout.bounds(MazeGameLayout.GAME_GRID), screenX, screenYFromBottom, gridSize);
    if (position.isEmpty()) {
      return GameInputAction.NONE;
    }
    if (button == Input.Buttons.LEFT) {
      return GameInputAction.cell(GameInputActionType.PLACE_WALL, position.get());
    }
    if (button == Input.Buttons.RIGHT) {
      return GameInputAction.cell(GameInputActionType.CLEAR_WALL, position.get());
    }
    return GameInputAction.of(GameInputActionType.IGNORED_GRID_CLICK);
  }

  private static boolean contains(
      ScreenLayout layout, String elementId, int screenX, float screenYFromBottom) {
    return layout.bounds(elementId).contains(screenX, screenYFromBottom);
  }

  private static Optional<GridPosition> gridPositionAt(
      ScreenRectangle grid, float pointX, float pointY, GridSize gridSize) {
    float cellSize = grid.width() / gridSize.columns();
    if (pointX < grid.x() || pointY < grid.y() || pointX >= grid.right() || pointY >= grid.top()) {
      return Optional.empty();
    }
    int column = (int) ((pointX - grid.x()) / cellSize);
    int rowFromBottom = (int) ((pointY - grid.y()) / cellSize);
    int row = gridSize.rows() - 1 - rowFromBottom;
    return Optional.of(new GridPosition(row, column));
  }
}
