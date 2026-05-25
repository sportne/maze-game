package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.Levels;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class MazeGameDebugHarnessTest {
  @Test
  void rejectsInvalidScreenDimensions() {
    assertThrows(IllegalArgumentException.class, () -> new MazeGameDebugHarness(0, 720));
    assertThrows(IllegalArgumentException.class, () -> new MazeGameDebugHarness(1280, 0));
  }

  @Test
  void simulatesWallPlacementAndClearingByGridCell() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();
    GridPosition wall = new GridPosition(2, 2);

    harness.leftClickCell(wall);
    assertTrue(harness.snapshot().mazeState().hasWallAt(wall));

    harness.rightClickCell(wall);
    assertTrue(harness.snapshot().mazeState().walls().isEmpty());
  }

  @Test
  void simulatesRejectedPlacementFeedback() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.leftClickCell(Levels.milestoneOne().mouseStart());

    assertEquals(Levels.milestoneOne().mouseStart(), harness.snapshot().rejectedPosition());
    assertTrue(harness.snapshot().mazeState().walls().isEmpty());
  }

  @Test
  void simulatesStartRetryAndReplayButtons() {
    MazeGameDebugHarness harness = new MazeGameDebugHarness();

    harness.clickStartRun().advance(Duration.ofSeconds(10));
    assertEquals(GamePhase.RESULT, harness.snapshot().gamePhase());

    harness.clickReplay();
    assertEquals(GamePhase.REPLAY, harness.snapshot().gamePhase());

    harness.advance(Duration.ofSeconds(10)).clickRetry();
    assertEquals(GamePhase.BUILDING, harness.snapshot().gamePhase());
  }
}
