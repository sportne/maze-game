package io.github.sportne.mazegame.layout;

import java.util.Objects;

/**
 * One frontend element with a stable id and rectangular contract.
 *
 * @param id stable identifier used by rendering, input, and tests
 * @param kind element category
 * @param bounds element rectangle in bottom-left coordinates
 * @param fitPolicy whether this element must fit in the viewport
 */
public record LayoutElement(
    String id, LayoutElementKind kind, ScreenRectangle bounds, LayoutFitPolicy fitPolicy) {
  /** Creates a validated layout element contract. */
  public LayoutElement {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("layout element id must be nonblank");
    }
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(bounds, "bounds");
    Objects.requireNonNull(fitPolicy, "fitPolicy");
  }
}
