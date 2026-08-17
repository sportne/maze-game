package io.github.sportne.mazegame.model.maze;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.solver.SolverRunStatus;
import org.junit.jupiter.api.Test;

final class ModelEnumTest {
  @Test
  void placeableCellTypesIncludeEverySupportedBuildTool() {
    assertArrayEquals(
        new PlaceableCellType[] {
          PlaceableCellType.WALL, PlaceableCellType.SLOW_FLOOR, PlaceableCellType.ALTERNATING_GATE
        },
        PlaceableCellType.values());
  }

  @Test
  void fixedCellTypesIncludeEveryAuthoredCellEffect() {
    assertArrayEquals(
        new FixedCellType[] {
          FixedCellType.WALL, FixedCellType.SLOW_FLOOR, FixedCellType.ALTERNATING_GATE
        },
        FixedCellType.values());
  }

  @Test
  void mazeEditStatusesCoverEveryTransactionalOutcome() {
    assertArrayEquals(
        new MazeEditStatus[] {
          MazeEditStatus.PLACED,
          MazeEditStatus.REPLACED,
          MazeEditStatus.REMOVED,
          MazeEditStatus.MOVED,
          MazeEditStatus.NO_OP,
          MazeEditStatus.REJECTED_OUTSIDE_GRID,
          MazeEditStatus.REJECTED_PROTECTED_CELL,
          MazeEditStatus.REJECTED_FIXED_CELL,
          MazeEditStatus.REJECTED_MISSING_SOURCE,
          MazeEditStatus.REJECTED_OCCUPIED_DESTINATION,
          MazeEditStatus.REJECTED_EXHAUSTED_SUPPLY,
          MazeEditStatus.REJECTED_BLOCKS_PATH
        },
        MazeEditStatus.values());
  }

  @Test
  void solverRunStatusesCoverRunningAndTerminalOutcomes() {
    assertArrayEquals(
        new SolverRunStatus[] {
          SolverRunStatus.RUNNING, SolverRunStatus.REACHED_GOAL, SolverRunStatus.TIMED_OUT
        },
        SolverRunStatus.values());
  }
}
