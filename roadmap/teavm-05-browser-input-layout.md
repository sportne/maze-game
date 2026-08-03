# WEB-05: Make Input and Layout Browser-Ready

Status: pending

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
