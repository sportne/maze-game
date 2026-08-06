# M2-01: Make Mobile Controls Genuinely Playable

Status: pending

Depends on: none

## Goal

Replace the current desktop canvas scaled down to phone dimensions with a responsive presentation
whose text, buttons, grid interactions, and game status remain practical to read and touch in both
portrait and landscape Safari.

## Scope

- Measure the rendered size and placement of every interactive control on representative phone
  portrait and toolbar-constrained landscape viewports.
- Define responsive layout modes that may reposition UI regions while preserving the existing game
  state and input actions.
- Provide practical touch targets and readable labels without changing desktop layout behavior.
- Project primary buttons to at least 44x44 CSS pixels and editable grid cells to at least 32x32 CSS
  pixels, with visible cell boundaries, in every supported mobile layout.
- Respect dynamic mobile viewport height and safe-area insets without requiring browser chrome to be
  hidden.
- Keep canvas coordinate conversion accurate after rotation, resize, device-pixel-ratio changes, and
  responsive layout changes.
- Apply the same behavior to the JavaScript default and WebAssembly preview.
- Update the viewport guidance so it appears only when no supported layout can remain usable.

## Acceptance Criteria

- A full game can be completed on an iPhone-sized 390x844 portrait viewport without controls being
  too small, clipped, overlapped, or obscured by browser chrome.
- The same flow works at approximately 844x286 in landscape with Safari's address and tab bars
  visible.
- Primary buttons measure at least 44x44 CSS pixels, editable grid cells measure at least 32x32 CSS
  pixels, and labels remain legible without page zoom.
- Rotation during any non-terminal game phase preserves state and leaves controls aligned with their
  hit regions.
- Desktop layouts and mouse input remain unchanged at their existing reference sizes.
- JavaScript and WebAssembly render and route input equivalently.

## Verification

- Add layout-level tests for portrait and constrained-landscape arrangements and overlap rules.
- Extend browser tests to interact with, rather than only render, the full flow in both phone
  orientations using touch input and assert the projected CSS size of buttons and grid cells.
- Exercise resize and rotation during menu, build, and result phases.
- Perform a real-device Safari check, retain portrait and landscape screenshots in the browser
  evidence artifact, and record the device, viewport measurements, and observations in this card's
  completion notes.
- Run `./gradlew spotlessApply qualityGate pagesBuild nativeImage` with GraalVM 21.
