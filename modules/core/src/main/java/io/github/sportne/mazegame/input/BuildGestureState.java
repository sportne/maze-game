package io.github.sportne.mazegame.input;

import io.github.sportne.mazegame.model.cell.PlaceableCellType;

/**
 * Immutable controller-owned state for one palette pointer gesture.
 *
 * @param pointerId owning pointer id
 * @param originType palette type where the press began
 * @param pressX initial CSS-pixel x coordinate
 * @param pressY initial CSS-pixel y coordinate
 * @param currentX latest CSS-pixel x coordinate
 * @param currentY latest CSS-pixel y coordinate
 * @param dragThresholdCrossed whether movement reached the drag threshold
 */
public record BuildGestureState(
    int pointerId,
    PlaceableCellType originType,
    float pressX,
    float pressY,
    float currentX,
    float currentY,
    boolean dragThresholdCrossed) {}
