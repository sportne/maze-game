package io.github.sportne.mazegame.teavm;

import io.github.sportne.mazegame.MazeGame;
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
import io.github.sportne.mazegame.runtime.MazeGameRuntimeConfiguration;
import io.github.sportne.mazegame.state.BestResultStore;
import io.github.sportne.mazegame.state.GameSession;
import java.time.Duration;
import java.util.List;
import java.util.OptionalLong;

/** Loopback-only level fixture used by real-browser build-gesture verification. */
final class BrowserBuildGestureFixture {
  static final String STORAGE_KEY = "maze-game.browser-build-gesture-fixture";
  static final String STORAGE_VALUE = "enabled";
  static final String LEVEL_ID = "browser-build-gesture-fixture";

  private BrowserBuildGestureFixture() {}

  /** Returns whether the browser host is local enough to permit test-fixture activation. */
  static boolean isLoopback(String hostName) {
    return "127.0.0.1".equals(hostName) || "localhost".equals(hostName) || "::1".equals(hostName);
  }

  /** Returns whether the local browser request explicitly selected the fixture. */
  static boolean requested(String hostName, String fixtureToken) {
    return isLoopback(hostName) && STORAGE_VALUE.equals(fixtureToken);
  }

  /** Creates a game whose single local-only level supplies both draggable cell types. */
  static MazeGame create(
      MazeGameRuntimeConfiguration runtimeConfiguration, BestResultStore bestResultStore) {
    LevelDefinition level = level();
    GameSession session =
        new GameSession(new LevelCatalog(List.of(level)), level.id(), bestResultStore);
    return new MazeGame(runtimeConfiguration, session);
  }

  /** Returns the shared fixture definition used by the browser launcher. */
  static LevelDefinition level() {
    return new LevelDefinition(
        LEVEL_ID,
        "Build Gesture Fixture",
        GridSize.square(5),
        Duration.ofSeconds(30),
        Duration.ofMillis(200),
        Duration.ofSeconds(3),
        Duration.ofMillis(50),
        List.of(
            PlaceableCellSupply.finite(PlaceableCellType.WALL, 2),
            PlaceableCellSupply.finite(PlaceableCellType.SLOW_FLOOR, 2)),
        List.of(
            new FixedCell(new GridPosition(1, 0), FixedCellType.WALL),
            new FixedCell(new GridPosition(1, 4), FixedCellType.SLOW_FLOOR)),
        List.of(
            new LevelSolver(
                new GridPosition(4, 2),
                new GridPosition(0, 2),
                SolverBehavior.LEAST_VISITED,
                OptionalLong.empty(),
                SolverAppearance.TRACKER_RACCOON,
                GoalType.TRASH_CAN),
            new LevelSolver(
                new GridPosition(4, 4),
                new GridPosition(0, 4),
                SolverBehavior.LINE_OF_SIGHT,
                OptionalLong.of(17L),
                SolverAppearance.SEEKER_RABBIT,
                GoalType.CARROT)));
  }
}
