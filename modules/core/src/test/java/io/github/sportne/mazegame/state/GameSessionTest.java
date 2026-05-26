package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.model.GamePhase;
import io.github.sportne.mazegame.model.GridPosition;
import io.github.sportne.mazegame.model.Levels;
import io.github.sportne.mazegame.model.MouseRunResult;
import io.github.sportne.mazegame.model.MouseRunStatus;
import org.junit.jupiter.api.Test;

final class GameSessionTest {
  @Test
  void startsAtMainMenuWithMilestoneOneDefaultsReady() {
    GameSession session = new GameSession();

    assertEquals(GamePhase.MAIN_MENU, session.gamePhase());
    assertEquals(Levels.milestoneOne(), session.levelDefinition());
    assertTrue(session.mazeState().walls().isEmpty());
    assertEquals(30.0F, session.buildTimeRemainingSeconds());
    assertFalse(session.runRequested());
    assertNull(session.mouseRunResult());
  }

  @Test
  void opensMenusOnlyFromMainMenu() {
    GameSession session = new GameSession();

    session.openLevelSelect();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());

    session.openSettings();
    assertEquals(GamePhase.LEVEL_SELECT, session.gamePhase());

    session.returnToMainMenu();
    session.openSettings();
    assertEquals(GamePhase.SETTINGS, session.gamePhase());
  }

  @Test
  void buildTimerAutoStartsRunAtZero() {
    GameSession session = startedSession();

    session.updateBuildTimer(31.0F);

    assertEquals(GamePhase.MOUSE_RUNNING, session.gamePhase());
    assertEquals(0.0F, session.buildTimeRemainingSeconds());
    assertTrue(session.runRequested());
    assertEquals(Levels.milestoneOne().mouseStart(), session.mouseRunResult().position());
  }

  @Test
  void wallPlacementAndClearingMutateOnlyDuringBuildPhase() {
    GameSession session = startedSession();
    GridPosition wall = new GridPosition(2, 2);

    session.placeWall(wall);
    assertTrue(session.mazeState().hasWallAt(wall));

    session.clearWall(wall);
    assertFalse(session.mazeState().hasWallAt(wall));

    session.startRun();
    session.placeWall(wall);
    assertFalse(session.mazeState().hasWallAt(wall));
  }

  @Test
  void rejectedPlacementFlashesAndExpires() {
    GameSession session = startedSession();

    session.placeWall(Levels.milestoneOne().mouseStart());
    assertEquals(Levels.milestoneOne().mouseStart(), session.rejectedPosition());
    assertEquals(0.5F, session.rejectedFlashRemainingSeconds());

    session.updateBuildTimer(0.5F);
    assertNull(session.rejectedPosition());
    assertEquals(0.0F, session.rejectedFlashRemainingSeconds());
  }

  @Test
  void startRunIsIgnoredOutsideBuildPhase() {
    GameSession session = new GameSession();

    session.startRun();

    assertEquals(GamePhase.MAIN_MENU, session.gamePhase());
    assertNull(session.mouseRunResult());
  }

  @Test
  void updateMouseRunMovesToResultWhenTerminal() {
    GameSession session = startedSession();

    session.startRun();
    session.updateMouseRun(10.0F);

    assertEquals(GamePhase.RESULT, session.gamePhase());
    assertEquals(MouseRunStatus.TIMED_OUT, session.mouseRunResult().status());
    assertTrue(session.resultPassed());
  }

  @Test
  void retryResetsTheLevel() {
    GameSession session = startedSession();
    GridPosition wall = new GridPosition(2, 2);
    session.placeWall(wall);
    session.startRun();
    session.updateMouseRun(10.0F);

    session.retryLevel();

    assertEquals(GamePhase.BUILDING, session.gamePhase());
    assertFalse(session.runRequested());
    assertTrue(session.mazeState().walls().isEmpty());
    assertNull(session.mouseRunResult());
  }

  @Test
  void replayUsesSameMazeAndSeed() {
    GameSession session = startedSession();
    session.startRun();
    session.updateMouseRun(10.0F);
    MouseRunResult firstResult = session.mouseRunResult();

    session.replayRun();
    session.updateMouseRun(10.0F);

    assertEquals(firstResult, session.mouseRunResult());
  }

  @Test
  void wallMutationsRequirePositions() {
    GameSession session = startedSession();

    assertThrows(NullPointerException.class, () -> session.placeWall(null));
    assertThrows(NullPointerException.class, () -> session.clearWall(null));
  }

  private static GameSession startedSession() {
    GameSession session = new GameSession();
    session.startMilestoneOneLevel();
    return session;
  }
}
