package io.github.sportne.mazegame.teavm;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class BrowserTouchGestureOwnerTest {
  @Test
  void ignoredSecondTouchCancellationDoesNotReleaseFirstOwner() {
    BrowserTouchGestureOwner owner = new BrowserTouchGestureOwner();
    owner.touchStarted(41);
    owner.claimPointer(0);
    owner.touchStarted(72);

    assertFalse(owner.release(72));
    assertTrue(owner.hasOwner());
    assertTrue(owner.release(41));
    assertFalse(owner.hasOwner());
  }

  @Test
  void batchedTouchesClaimIdentifierMappedToActualControllerPointer() {
    BrowserTouchGestureOwner owner = new BrowserTouchGestureOwner();
    owner.touchStarted(41);
    owner.touchStarted(72);
    owner.claimPointer(1);

    assertFalse(owner.release(41));
    assertTrue(owner.hasOwner());
    assertTrue(owner.release(72));
    assertFalse(owner.hasOwner());
  }

  @Test
  void lifecycleCancellationClearsOwnershipButPreservesActiveTouchMappings() {
    BrowserTouchGestureOwner owner = new BrowserTouchGestureOwner();
    owner.touchStarted(41);
    owner.touchStarted(72);
    owner.claimPointer(1);

    owner.clearOwnership();

    assertFalse(owner.hasOwner());
    assertFalse(owner.release(41));
    owner.touchStarted(99);
    owner.claimPointer(0);
    assertTrue(owner.release(99));
    assertFalse(owner.release(72));
  }

  @Test
  void canceledOwnerLeavesSurvivingPointerAssignmentsAlignedWithBackend() {
    BrowserTouchGestureOwner owner = new BrowserTouchGestureOwner();
    owner.touchStarted(41);
    owner.touchStarted(72);
    owner.claimPointer(1);

    assertTrue(owner.release(72));
    owner.clearOwnership();
    owner.touchStarted(99);
    owner.claimPointer(1);

    assertTrue(owner.release(99));
    assertFalse(owner.release(41));
  }
}
