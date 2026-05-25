package io.github.sportne.mazegame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.badlogic.gdx.graphics.Color;
import org.junit.jupiter.api.Test;

final class MazeGameTest {
  @Test
  void gameCanBeConstructed() {
    assertNotNull(new MazeGame());
  }

  @Test
  void titleProvidesTheDesktopWindowTitle() {
    assertEquals("Maze Game", MazeGame.title());
  }

  @Test
  void backgroundProvidesTheInitialClearColor() {
    assertEquals(new Color(0.07F, 0.08F, 0.10F, 1.0F), MazeGame.background());
  }
}
