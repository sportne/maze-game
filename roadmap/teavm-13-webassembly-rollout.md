# WEB-13: Evaluate and Roll Out WebAssembly

Status: pending

Depends on: WEB-11A, WEB-12

## Goal

Use measured compatibility and performance to decide whether WebAssembly should replace,
supplement, or remain secondary to the JavaScript build.

## Scope

- Compare compressed size, startup time, first rendered frame, frame pacing, and memory use.
- Run the same supported-browser and touch-input matrix used for the JavaScript release.
- Assess debugging, source-map, hosting, and cache behavior on GitHub Pages.
- Choose one rollout mode: WebAssembly default with JavaScript fallback, user-selectable builds, or
  JavaScript default with WebAssembly preview.
- Add feature detection and fallback only if WebAssembly becomes a production entry point.
- Document the decision and retain metrics as a future regression baseline.

## Acceptance Criteria

- The rollout choice is supported by recorded measurements and browser results.
- Unsupported clients receive a working JavaScript experience rather than a blank page.
- Pages deployment remains atomic and rollback-capable.
- CI verifies every production browser artifact.

## Verification

- Run both production artifacts from the same local and GitHub Pages path structure.
- Run the complete browser matrix and automated smoke suite.
- Confirm fallback behavior by deliberately disabling WebAssembly.
