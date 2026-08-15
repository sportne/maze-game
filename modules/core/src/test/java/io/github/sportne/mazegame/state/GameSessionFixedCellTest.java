package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.cell.FixedCellType;
import io.github.sportne.mazegame.model.cell.PlaceableCellSupply;
import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import io.github.sportne.mazegame.model.grid.GridPosition;
import io.github.sportne.mazegame.model.grid.GridSize;
import io.github.sportne.mazegame.model.level.FixedCell;
import io.github.sportne.mazegame.model.level.GoalType;
import io.github.sportne.mazegame.model.level.LevelCatalog;
import io.github.sportne.mazegame.model.level.LevelDefinition;
import io.github.sportne.mazegame.model.level.LevelSolver;
import io.github.sportne.mazegame.model.level.SolverAppearance;
import io.github.sportne.mazegame.model.level.SolverBehavior;
import io.github.sportne.mazegame.model.maze.CellContent;
import io.github.sportne.mazegame.model.maze.MazeEditResult;
import io.github.sportne.mazegame.model.maze.MazeEditStatus;
import io.github.sportne.mazegame.model.maze.MazeState;
import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

final class GameSessionFixedCellTest {
  private static final GridPosition FIXED_WALL = new GridPosition(1, 0);
  private static final GridPosition FIXED_SLOW_FLOOR = new GridPosition(1, 2);

  @Test
  void fixedCellsPersistAcrossEditingNavigationRunReplayAndRetry() {
    LevelDefinition level = level();
    GameSession session =
        new GameSession(new LevelCatalog(List.of(level)), level.id(), BestResultStore.none());
    assertFixedCells(session.mazeState());

    assertTrue(session.startLevel(level.id()));
    MazeState initial = session.mazeState();
    MazeEditResult rejected = session.placeOrReplaceCell(FIXED_WALL).orElseThrow();
    assertEquals(MazeEditStatus.REJECTED_FIXED_CELL, rejected.status());
    assertSame(initial, session.mazeState());
    assertEquals(FIXED_WALL, session.rejectedPosition());

    session.returnToLevelSelect();
    assertFixedCells(session.mazeState());
    assertTrue(session.startLevel(level.id()));
    session.startRun();
    assertFixedCells(session.mazeState());
    session.updateGame(5.0F);
    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertFixedCells(session.mazeState());

    session.replayRun();
    assertEquals(GamePhase.REPLAY, session.gamePhase());
    assertFixedCells(session.mazeState());
    session.updateGame(5.0F);
    session.retryLevel();
    assertEquals(GamePhase.BUILDING, session.gamePhase());
    assertTrue(session.mazeState().placedCells().isEmpty());
    assertFixedCells(session.mazeState());

    session.returnToMainMenu();
    assertEquals(GamePhase.MAIN_MENU, session.gamePhase());
    assertFixedCells(session.mazeState());
  }

  private static void assertFixedCells(MazeState mazeState) {
    assertEquals(CellContent.NORMAL_WALL, mazeState.cellContentAt(FIXED_WALL));
    assertEquals(CellContent.SLOW_FLOOR, mazeState.cellContentAt(FIXED_SLOW_FLOOR));
  }

  private static LevelDefinition level() {
    return new LevelDefinition(
        "fixed-session",
        "Fixed Session",
        GridSize.square(3),
        Duration.ofSeconds(10),
        Duration.ofSeconds(2),
        Duration.ofSeconds(5),
        Duration.ofMillis(250),
        List.of(
            PlaceableCellSupply.infinite(PlaceableCellType.WALL),
            PlaceableCellSupply.infinite(PlaceableCellType.SLOW_FLOOR)),
        List.of(
            new FixedCell(FIXED_WALL, FixedCellType.WALL),
            new FixedCell(FIXED_SLOW_FLOOR, FixedCellType.SLOW_FLOOR)),
        List.of(
            new LevelSolver(
                new GridPosition(2, 1),
                new GridPosition(0, 1),
                SolverBehavior.LEFT_PRIORITY,
                OptionalLong.empty(),
                SolverAppearance.SCOUT_SQUIRREL,
                GoalType.ACORN)));
  }
}
