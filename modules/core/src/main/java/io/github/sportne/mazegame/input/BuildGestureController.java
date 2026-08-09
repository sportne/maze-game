package io.github.sportne.mazegame.input;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;
import java.util.Objects;
import java.util.Optional;

/** Owns the transient pointer state used to distinguish palette selection from palette dragging. */
public final class BuildGestureController {
  /** Drag threshold measured in normalized CSS pixels. */
  public static final float DRAG_THRESHOLD_CSS_PIXELS = 8.0F;

  /** Active gesture, or null while idle. */
  private BuildGestureState state;

  /** Creates an idle gesture controller. */
  public BuildGestureController() {
    state = null;
  }

  /**
   * Starts a palette press when no pointer already owns the controller.
   *
   * @param pointerId pointer attempting to own the gesture
   * @param originType pressed palette type
   * @param inputX x coordinate in input-device pixels
   * @param inputY y coordinate in input-device pixels
   * @param pixelsPerCssPixel input-device pixels represented by one CSS pixel
   * @return true when this pointer acquired ownership
   */
  public boolean press(
      int pointerId,
      PlaceableCellType originType,
      float inputX,
      float inputY,
      float pixelsPerCssPixel) {
    Objects.requireNonNull(originType, "originType");
    validateScale(pixelsPerCssPixel);
    if (state != null) {
      return false;
    }
    float cssX = inputX / pixelsPerCssPixel;
    float cssY = inputY / pixelsPerCssPixel;
    state = new BuildGestureState(pointerId, originType, cssX, cssY, cssX, cssY, false);
    return true;
  }

  /**
   * Updates the active pointer coordinate and threshold state.
   *
   * @param pointerId moving pointer id
   * @param inputX x coordinate in input-device pixels
   * @param inputY y coordinate in input-device pixels
   * @param pixelsPerCssPixel input-device pixels represented by one CSS pixel
   * @return true when this pointer owns the gesture
   */
  public boolean move(int pointerId, float inputX, float inputY, float pixelsPerCssPixel) {
    validateScale(pixelsPerCssPixel);
    if (!owns(pointerId)) {
      return false;
    }
    state = updatedState(inputX / pixelsPerCssPixel, inputY / pixelsPerCssPixel);
    return true;
  }

  /**
   * Releases the owning pointer and returns its final state.
   *
   * @param pointerId releasing pointer id
   * @param inputX x coordinate in input-device pixels
   * @param inputY y coordinate in input-device pixels
   * @param pixelsPerCssPixel input-device pixels represented by one CSS pixel
   * @return final gesture state, or empty when another pointer released
   */
  public Optional<BuildGestureState> release(
      int pointerId, float inputX, float inputY, float pixelsPerCssPixel) {
    validateScale(pixelsPerCssPixel);
    if (!owns(pointerId)) {
      return Optional.empty();
    }
    BuildGestureState released =
        updatedState(inputX / pixelsPerCssPixel, inputY / pixelsPerCssPixel);
    state = null;
    return Optional.of(released);
  }

  /** Clears any active gesture without producing an edit. */
  public void cancel() {
    state = null;
  }

  /**
   * Returns whether a pointer currently owns the gesture.
   *
   * @param pointerId pointer id to check
   * @return true when the pointer owns the gesture
   */
  public boolean owns(int pointerId) {
    return state != null && state.pointerId() == pointerId;
  }

  /**
   * Returns the active immutable gesture state.
   *
   * @return current state, or empty while idle
   */
  public Optional<BuildGestureState> state() {
    return Optional.ofNullable(state);
  }

  /**
   * Returns whether the owning pointer has crossed the threshold and is captured as a drag.
   *
   * @return true only during an active drag
   */
  public boolean pointerCaptured() {
    return state != null && state.dragThresholdCrossed();
  }

  private BuildGestureState updatedState(float cssX, float cssY) {
    float deltaX = cssX - state.pressX();
    float deltaY = cssY - state.pressY();
    float thresholdSquared = DRAG_THRESHOLD_CSS_PIXELS * DRAG_THRESHOLD_CSS_PIXELS;
    boolean thresholdCrossed =
        state.dragThresholdCrossed() || deltaX * deltaX + deltaY * deltaY >= thresholdSquared;
    return new BuildGestureState(
        state.pointerId(),
        state.originType(),
        state.pressX(),
        state.pressY(),
        cssX,
        cssY,
        thresholdCrossed);
  }

  private static void validateScale(float pixelsPerCssPixel) {
    if (!Float.isFinite(pixelsPerCssPixel) || pixelsPerCssPixel <= 0.0F) {
      throw new IllegalArgumentException("pixelsPerCssPixel must be finite and positive");
    }
  }
}
