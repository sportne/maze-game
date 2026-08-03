# WEB-05: Make Input and Layout Browser-Ready

Status: complete

Depends on: WEB-04

## Goal

Provide reliable gameplay across desktop browser sizes and a usable touch interaction path.

## Scope

- Prevent the canvas context menu from interfering with right-click wall clearing.
- Verify pointer coordinates through the fit viewport at different CSS and device-pixel sizes.
- Add a visible clear-wall mode or equivalent touch-safe interaction that does not depend on a
  secondary mouse button.
- Make the web canvas fill its host while retaining the 16:9 game viewport.
- Define a minimum usable viewport and display guidance when the viewport is too small.
- Test high-DPI behavior and browser zoom.

## Acceptance Criteria

- Left click/tap can place walls and a discoverable action can clear walls.
- Desktop right-click clearing remains available without opening a context menu.
- Controls route correctly after resize, zoom, and device-pixel-ratio changes.
- The complete game loop is usable with both mouse and touch input.
- Desktop interaction behavior is not regressed.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew webBuild`
- Manual viewport matrix at 1280x720, 1024x768, 390x844, and a high-DPI desktop viewport.

## Completion Notes

Completed on 2026-08-03.

- Added a visible Place/Clear wall mode so a primary mouse click or touch can perform either build
  action, while retaining desktop right-click clearing and resetting the mode for fresh attempts.
- Added browser page setup for mobile viewport sizing, physical-pixel canvas backing, touch gesture
  ownership, canvas-scoped context-menu suppression, and resize-aware minimum-viewport guidance.
- Defined 640x360 landscape as the minimum usable viewport; smaller and portrait windows receive a
  clear rotate-or-resize message instead of undersized controls.
- Passed `spotlessApply`, the full `qualityGate`, and the optimized `webBuild`.
- Verified in headless Chromium at 1280x720, post-resize 1024x768, touch 844x390, portrait 390x844,
  DPR2 1280x720, and a 125% zoom-equivalent 1024x576 CSS viewport backed by 1280x720 pixels. Wall
  placement and both clear paths produced the expected pixels with no page, console, asset, or
  request errors.
- Received approval from both general and simplicity-focused reviewers with no remaining findings.
