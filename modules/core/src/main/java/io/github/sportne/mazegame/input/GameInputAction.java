package io.github.sportne.mazegame.input;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.Objects;

/**
 * Routed input intent plus the payload needed to apply it.
 *
 * @param type action type
 * @param position clicked grid cell for cell actions, otherwise null
 * @param levelId selected stable level id for level-selection actions, otherwise null
 * @param cellType selected palette type for palette actions, otherwise null
 */
public record GameInputAction(
    GameInputActionType type, GridPosition position, String levelId, PlaceableCellType cellType) {
  /** Shared no-op action. */
  public static final GameInputAction NONE =
      new GameInputAction(GameInputActionType.NONE, null, null, null);

  /**
   * Creates an action without a palette-type payload for compatibility with non-palette callers.
   */
  public GameInputAction(GameInputActionType type, GridPosition position, String levelId) {
    this(type, position, levelId, null);
  }

  /** Creates an input action with a valid payload for its type. */
  public GameInputAction {
    Objects.requireNonNull(type, "type");
    boolean cellAction =
        type == GameInputActionType.PLACE_OR_REPLACE_CELL
            || type == GameInputActionType.REMOVE_CELL;
    boolean levelAction = type == GameInputActionType.SELECT_LEVEL;
    boolean paletteAction = type == GameInputActionType.SELECT_CELL_TYPE;
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
    if (paletteAction && cellType == null) {
      throw new IllegalArgumentException("palette-selection actions require a cell type");
    }
    if (!paletteAction && cellType != null) {
      throw new IllegalArgumentException("only palette-selection actions can carry a cell type");
    }
  }

  /**
   * Creates an action with no cell payload.
   *
   * @param type action type
   * @return action
   */
  public static GameInputAction of(GameInputActionType type) {
    return type == GameInputActionType.NONE ? NONE : new GameInputAction(type, null, null, null);
  }

  /**
   * Creates a cell action.
   *
   * @param type cell action type
   * @param position clicked grid cell
   * @return action
   */
  public static GameInputAction cell(GameInputActionType type, GridPosition position) {
    return new GameInputAction(type, position, null, null);
  }

  /**
   * Creates a level-selection action.
   *
   * @param levelId selected stable level id
   * @return action
   */
  public static GameInputAction selectLevel(String levelId) {
    return new GameInputAction(GameInputActionType.SELECT_LEVEL, null, levelId, null);
  }

  /** Creates a build-palette selection action. */
  public static GameInputAction selectCellType(PlaceableCellType cellType) {
    return new GameInputAction(GameInputActionType.SELECT_CELL_TYPE, null, null, cellType);
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
