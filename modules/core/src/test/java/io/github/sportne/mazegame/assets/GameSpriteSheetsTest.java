package io.github.sportne.mazegame.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.sportne.mazegame.model.solver.CardinalDirection;
import org.junit.jupiter.api.Test;

final class GameSpriteSheetsTest {
  @Test
  void cheeseUsesTheFirstGoalFrame() {
    assertRegion(GameSpriteSheets.cheese(new TestTexture()), 0, 0);
  }

  @Test
  void acornUsesTheFifthGoalFrame() {
    assertRegion(GameSpriteSheets.acorn(new TestTexture()), 512, 0);
  }

  @Test
  void randomBehaviorUsesTheRightFacingClassicMouse() {
    assertRegion(GameSpriteSheets.randomSolver(new TestTexture()), 384, 0);
  }

  @Test
  void scoutBehaviorUsesTheRightFacingBasicSquirrel() {
    assertRegion(GameSpriteSheets.scoutSquirrel(new TestTexture()), 384, 384);
  }

  @Test
  void randomBehaviorMapsGridDirectionsToClassicMouseColumns() {
    DirectionalSpriteSet sprites = GameSpriteSheets.randomSolverSprites(new TestTexture());

    assertRegion(sprites.sprite(CardinalDirection.SOUTH), 0, 0);
    assertRegion(sprites.sprite(CardinalDirection.NORTH), 128, 0);
    assertRegion(sprites.sprite(CardinalDirection.WEST), 256, 0);
    assertRegion(sprites.sprite(CardinalDirection.EAST), 384, 0);
    assertRegion(sprites.defaultSprite(), 384, 0);
  }

  @Test
  void scoutBehaviorMapsGridDirectionsToBasicSquirrelColumns() {
    DirectionalSpriteSet sprites = GameSpriteSheets.scoutSquirrelSprites(new TestTexture());

    assertRegion(sprites.sprite(CardinalDirection.SOUTH), 0, 384);
    assertRegion(sprites.sprite(CardinalDirection.NORTH), 128, 384);
    assertRegion(sprites.sprite(CardinalDirection.WEST), 256, 384);
    assertRegion(sprites.sprite(CardinalDirection.EAST), 384, 384);
  }

  private static void assertRegion(TextureRegion region, int expectedX, int expectedY) {
    assertEquals(expectedX, region.getRegionX());
    assertEquals(expectedY, region.getRegionY());
    assertEquals(128, region.getRegionWidth());
    assertEquals(128, region.getRegionHeight());
  }

  private static final class TestTexture extends Texture {
    private TestTexture() {
      super();
    }

    @Override
    public int getWidth() {
      return 640;
    }

    @Override
    public int getHeight() {
      return 512;
    }
  }
}
