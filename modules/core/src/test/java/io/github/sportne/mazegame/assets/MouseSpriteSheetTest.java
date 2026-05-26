package io.github.sportne.mazegame.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.junit.jupiter.api.Test;

final class MouseSpriteSheetTest {
  @Test
  void cheeseRegionUsesExpectedSpriteSheetCoordinates() {
    TextureRegion cheese = MouseSpriteSheet.cheese(new TestTexture());

    assertEquals(1168, cheese.getRegionX());
    assertEquals(819, cheese.getRegionY());
    assertEquals(186, cheese.getRegionWidth());
    assertEquals(145, cheese.getRegionHeight());
  }

  @Test
  void mouseRegionUsesExpectedSpriteSheetCoordinates() {
    TextureRegion mouse = MouseSpriteSheet.mouse(new TestTexture());

    assertEquals(718, mouse.getRegionX());
    assertEquals(671, mouse.getRegionY());
    assertEquals(325, mouse.getRegionWidth());
    assertEquals(416, mouse.getRegionHeight());
  }

  private static final class TestTexture extends Texture {
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 1000;

    private TestTexture() {
      super();
    }

    @Override
    public int getWidth() {
      return WIDTH;
    }

    @Override
    public int getHeight() {
      return HEIGHT;
    }
  }
}
