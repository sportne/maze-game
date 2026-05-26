package io.github.sportne.mazegame.input;

import io.github.sportne.mazegame.model.GridPosition;
import java.util.Objects;

/**
 * Routed input intent plus any grid-cell payload needed to apply it.
 *
 * @param type action type
 * @param position clicked grid cell for cell actions, otherwise null
 */
public record GameInputAction(GameInputActionType type, GridPosition position) {
  /** Shared no-op action. */
  public static final GameInputAction NONE = new GameInputAction(GameInputActionType.NONE, null);

  /** Creates an input action with a valid payload for its type. */
  public GameInputAction {
    Objects.requireNonNull(type, "type");
    boolean cellAction =
        type == GameInputActionType.PLACE_WALL || type == GameInputActionType.CLEAR_WALL;
    if (cellAction && position == null) {
      throw new IllegalArgumentException("cell actions require a position");
    }
    if (!cellAction && position != null) {
      throw new IllegalArgumentException("only cell actions can carry a position");
    }
  }

  /**
   * Creates an action with no cell payload.
   *
   * @param type action type
   * @return action
   */
  public static GameInputAction of(GameInputActionType type) {
    return type == GameInputActionType.NONE ? NONE : new GameInputAction(type, null);
  }

  /**
   * Creates a cell action.
   *
   * @param type cell action type
   * @param position clicked grid cell
   * @return action
   */
  public static GameInputAction cell(GameInputActionType type, GridPosition position) {
    return new GameInputAction(type, position);
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
