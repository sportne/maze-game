package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.List;

/** Absolute grid direction with Scout's relative candidate ordering. */
enum CardinalDirection {
  NORTH(-1, 0),
  EAST(0, 1),
  SOUTH(1, 0),
  WEST(0, -1);

  private final int rowChange;
  private final int columnChange;

  CardinalDirection(int rowChange, int columnChange) {
    this.rowChange = rowChange;
    this.columnChange = columnChange;
  }

  List<CardinalDirection> leftStraightRightBack() {
    return switch (this) {
      case NORTH -> List.of(WEST, NORTH, EAST, SOUTH);
      case EAST -> List.of(NORTH, EAST, SOUTH, WEST);
      case SOUTH -> List.of(EAST, SOUTH, WEST, NORTH);
      case WEST -> List.of(SOUTH, WEST, NORTH, EAST);
    };
  }

  GridPosition move(GridPosition origin) {
    return new GridPosition(origin.row() + rowChange, origin.column() + columnChange);
  }
}
