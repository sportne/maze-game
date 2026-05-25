package io.github.sportne.mazegame.layout;

import io.github.sportne.mazegame.model.GamePhase;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Complete declared layout for one game phase.
 *
 * @param phase phase this layout represents
 * @param viewport screen rectangle that contains the layout
 * @param elements declared frontend elements
 */
public record ScreenLayout(
    GamePhase phase, ScreenRectangle viewport, List<LayoutElement> elements) {
  /** Creates a layout with an immutable element list. */
  public ScreenLayout {
    Objects.requireNonNull(phase, "phase");
    Objects.requireNonNull(viewport, "viewport");
    elements = List.copyOf(elements);
  }

  /**
   * Finds an element by id.
   *
   * @param id stable element id
   * @return matching element, or empty
   */
  public Optional<LayoutElement> element(String id) {
    return elements.stream().filter(element -> element.id().equals(id)).findFirst();
  }

  /**
   * Returns a required element by id.
   *
   * @param id stable element id
   * @return matching element
   * @throws IllegalArgumentException when the id is not present
   */
  public LayoutElement requiredElement(String id) {
    return element(id).orElseThrow(() -> new IllegalArgumentException("missing layout id " + id));
  }

  /**
   * Returns a required element's bounds.
   *
   * @param id stable element id
   * @return matching element bounds
   */
  public ScreenRectangle bounds(String id) {
    return requiredElement(id).bounds();
  }
}
