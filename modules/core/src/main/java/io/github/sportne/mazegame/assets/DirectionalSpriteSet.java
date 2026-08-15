package io.github.sportne.mazegame.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.sportne.mazegame.model.solver.CardinalDirection;
import java.util.Objects;

/** Four directional frames for one solver character appearance. */
public final class DirectionalSpriteSet {
  private final TextureRegion front;
  private final TextureRegion back;
  private final TextureRegion left;
  private final TextureRegion right;

  /** Creates a set in the processed sprite sheets' front, back, left, right column order. */
  public DirectionalSpriteSet(
      TextureRegion front, TextureRegion back, TextureRegion left, TextureRegion right) {
    this.front = copy(front, "front");
    this.back = copy(back, "back");
    this.left = copy(left, "left");
    this.right = copy(right, "right");
  }

  /** Creates a compatibility set that uses one frame for every direction. */
  public static DirectionalSpriteSet single(TextureRegion sprite) {
    Objects.requireNonNull(sprite, "sprite");
    return new DirectionalSpriteSet(sprite, sprite, sprite, sprite);
  }

  /** Selects the visual frame matching an absolute grid movement direction. */
  public TextureRegion sprite(CardinalDirection direction) {
    TextureRegion selected =
        switch (Objects.requireNonNull(direction, "direction")) {
          case NORTH -> back;
          case EAST -> right;
          case SOUTH -> front;
          case WEST -> left;
        };
    return new TextureRegion(selected);
  }

  /** Returns the legacy right-facing frame used before a solver first moves. */
  public TextureRegion defaultSprite() {
    return new TextureRegion(right);
  }

  private static TextureRegion copy(TextureRegion region, String name) {
    return new TextureRegion(Objects.requireNonNull(region, name));
  }
}
