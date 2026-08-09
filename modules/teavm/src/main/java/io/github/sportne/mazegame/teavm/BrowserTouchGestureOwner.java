package io.github.sportne.mazegame.teavm;

import java.util.HashMap;
import java.util.Map;

/** Tracks the browser touch identifier corresponding to the controller's first owning pointer. */
final class BrowserTouchGestureOwner {
  private static final int MAX_TOUCHES = 20;
  private final Map<Integer, Integer> pointerByTouchIdentifier = new HashMap<>();
  private Integer touchIdentifier;

  /** Mirrors TeaVM's lowest-free pointer assignment for a newly started browser touch. */
  void touchStarted(int identifier) {
    if (pointerByTouchIdentifier.containsKey(identifier)) {
      return;
    }
    for (int pointer = 0; pointer < MAX_TOUCHES; pointer++) {
      if (!pointerByTouchIdentifier.containsValue(pointer)) {
        pointerByTouchIdentifier.put(identifier, pointer);
        return;
      }
    }
  }

  /** Claims the browser identifier mapped to the controller's actual libGDX pointer id. */
  void claimPointer(int pointerId) {
    if (touchIdentifier != null) {
      return;
    }
    for (Map.Entry<Integer, Integer> entry : pointerByTouchIdentifier.entrySet()) {
      if (entry.getValue() == pointerId) {
        touchIdentifier = entry.getKey();
        return;
      }
    }
  }

  /**
   * Releases a browser touch when it owns the gesture.
   *
   * @return true only when the released identifier was the owner
   */
  boolean release(int identifier) {
    pointerByTouchIdentifier.remove(identifier);
    boolean releasedOwner = touchIdentifier != null && touchIdentifier == identifier;
    if (releasedOwner) {
      touchIdentifier = null;
    }
    return releasedOwner;
  }

  /** Clears the gesture claim while preserving mappings for browser touches that remain active. */
  void clearOwnership() {
    touchIdentifier = null;
  }

  /** Returns whether a browser touch currently owns the palette gesture. */
  boolean hasOwner() {
    return touchIdentifier != null;
  }
}
