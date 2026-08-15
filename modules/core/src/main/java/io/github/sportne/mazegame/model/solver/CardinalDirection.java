package io.github.sportne.mazegame.model.solver;

import io.github.sportne.mazegame.model.grid.GridPosition;
import java.util.List;
import java.util.Objects;

/** Absolute grid movement direction used by simulation and character presentation. */
public enum CardinalDirection {
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

  static CardinalDirection between(GridPosition origin, GridPosition destination) {
    Objects.requireNonNull(origin, "origin");
    Objects.requireNonNull(destination, "destination");
    int rowDifference = destination.row() - origin.row();
    int columnDifference = destination.column() - origin.column();
    for (CardinalDirection direction : values()) {
      if (direction.rowChange == rowDifference && direction.columnChange == columnDifference) {
        return direction;
      }
    }
    throw new IllegalArgumentException("movement must be to an orthogonally adjacent cell");
  }
}
