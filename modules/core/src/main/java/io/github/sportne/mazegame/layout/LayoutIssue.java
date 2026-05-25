package io.github.sportne.mazegame.layout;

import java.util.Objects;

/**
 * One validation problem for a declared screen layout.
 *
 * @param type problem category
 * @param message human-readable description
 */
public record LayoutIssue(LayoutIssueType type, String message) {
  /** Creates a validated issue. */
  public LayoutIssue {
    Objects.requireNonNull(type, "type");
    if (message == null || message.isBlank()) {
      throw new IllegalArgumentException("layout issue message must be nonblank");
    }
  }
}
