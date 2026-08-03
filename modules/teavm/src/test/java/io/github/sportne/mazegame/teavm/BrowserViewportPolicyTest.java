package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class BrowserViewportPolicyTest {
  @Test
  void acceptsSupportedLandscapeViewports() {
    assertFalse(BrowserViewportPolicy.requiresGuidance(640, 360));
    assertFalse(BrowserViewportPolicy.requiresGuidance(1024, 768));
    assertFalse(BrowserViewportPolicy.requiresGuidance(1280, 720));
  }

  @Test
  void requestsMoreSpaceForSmallOrPortraitViewports() {
    assertTrue(BrowserViewportPolicy.requiresGuidance(639, 360));
    assertTrue(BrowserViewportPolicy.requiresGuidance(640, 359));
    assertTrue(BrowserViewportPolicy.requiresGuidance(390, 844));
  }
}
