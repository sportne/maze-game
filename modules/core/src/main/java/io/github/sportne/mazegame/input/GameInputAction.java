package io.github.sportne.mazegame.input;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;

/**
 * Routed input intent plus the payload needed to apply it.
 *
 * @param type action type
 * @param position clicked grid cell for cell actions, otherwise null
 * @param levelId selected stable level id for level-selection actions, otherwise null
 */
public record GameInputAction(GameInputActionType type, GridPosition position, String levelId) {
  /** Shared no-op action. */
  public static final GameInputAction NONE =
      new GameInputAction(GameInputActionType.NONE, null, null);

  /** Creates an input action with a valid payload for its type. */
  public GameInputAction {
    Objects.requireNonNull(type, "type");
    boolean cellAction =
        type == GameInputActionType.TOGGLE_WALL || type == GameInputActionType.CLEAR_WALL;
    boolean levelAction = type == GameInputActionType.SELECT_LEVEL;
    if (cellAction && position == null) {
      throw new IllegalArgumentException("cell actions require a position");
    }
    if (!cellAction && position != null) {
      throw new IllegalArgumentException("only cell actions can carry a position");
    }
    if (levelAction && (levelId == null || levelId.isBlank())) {
      throw new IllegalArgumentException("level-selection actions require a level id");
    }
    if (!levelAction && levelId != null) {
      throw new IllegalArgumentException("only level-selection actions can carry a level id");
    }
  }

  /**
   * Creates an action with no cell payload.
   *
   * @param type action type
   * @return action
   */
  public static GameInputAction of(GameInputActionType type) {
    return type == GameInputActionType.NONE ? NONE : new GameInputAction(type, null, null);
  }

  /**
   * Creates a cell action.
   *
   * @param type cell action type
   * @param position clicked grid cell
   * @return action
   */
  public static GameInputAction cell(GameInputActionType type, GridPosition position) {
    return new GameInputAction(type, position, null);
  }

  /**
   * Creates a level-selection action.
   *
   * @param levelId selected stable level id
   * @return action
   */
  public static GameInputAction selectLevel(String levelId) {
    return new GameInputAction(GameInputActionType.SELECT_LEVEL, null, levelId);
  }

  /**
   * Returns whether this action should consume the click.
   *
   * @return true when the routed click hit a recognized control or cell
   */
  public boolean consumed() {
    return type != GameInputActionType.NONE;
  }
}
