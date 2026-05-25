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
          GamePhase.BUILDING, GamePhase.MOUSE_RUNNING, GamePhase.RESULT, GamePhase.REPLAY
        },
        GamePhase.values());
  }
}
