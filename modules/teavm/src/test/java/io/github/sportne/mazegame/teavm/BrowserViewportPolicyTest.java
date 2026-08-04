package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class BrowserViewportPolicyTest {
  @Test
  void acceptsSupportedPortraitAndLandscapeViewports() {
    assertFalse(BrowserViewportPolicy.requiresGuidance(568, 270));
    assertFalse(BrowserViewportPolicy.requiresGuidance(390, 844));
    assertFalse(BrowserViewportPolicy.requiresGuidance(844, 286));
  }

  @Test
  void requestsMoreSpaceOnlyForUnusableViewports() {
    assertTrue(BrowserViewportPolicy.requiresGuidance(567, 270));
    assertTrue(BrowserViewportPolicy.requiresGuidance(568, 269));
  }
}
