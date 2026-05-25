package io.github.sportne.mazegame.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

final class ModelEnumTest {
  @Test
  void wallTypesIncludeNormalWallsOnlyForMilestoneOne() {
    assertArrayEquals(new WallType[] {WallType.NORMAL}, WallType.values());
  }

  @Test
  void gamePhasesCoverTheMilestoneOneLoop() {
    assertArrayEquals(
        new GamePhase[] {
          GamePhase.MAIN_MENU,
          GamePhase.LEVEL_SELECT,
          GamePhase.SETTINGS,
          GamePhase.BUILDING,
          GamePhase.MOUSE_RUNNING,
          GamePhase.RESULT,
          GamePhase.REPLAY
        },
        GamePhase.values());
  }

  @Test
  void wallPlacementStatusesCoverAcceptedAndRejectedOutcomes() {
    assertArrayEquals(
        new WallPlacementStatus[] {
          WallPlacementStatus.PLACED,
          WallPlacementStatus.ALREADY_PRESENT,
          WallPlacementStatus.REJECTED_OUTSIDE_GRID,
          WallPlacementStatus.REJECTED_PROTECTED_CELL,
          WallPlacementStatus.REJECTED_BLOCKS_PATH
        },
        WallPlacementStatus.values());
  }

  @Test
  void mouseRunStatusesCoverRunningAndTerminalOutcomes() {
    assertArrayEquals(
        new MouseRunStatus[] {
          MouseRunStatus.RUNNING, MouseRunStatus.REACHED_CHEESE, MouseRunStatus.TIMED_OUT
        },
        MouseRunStatus.values());
  }
}
