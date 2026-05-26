package io.github.sportne.mazegame.state;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

final class GamePhaseTest {
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
}
