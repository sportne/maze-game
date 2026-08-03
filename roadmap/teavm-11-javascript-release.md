# WEB-11: Validate and Document the JavaScript Release

Status: pending

Depends on: WEB-10

## Goal

Declare the JavaScript website supported only after production behavior and maintenance procedures
are documented and verified.

## Scope

- Run the release matrix on current Chrome, Firefox, Edge, Safari, and representative touch input.
- Verify direct navigation, reload, cache refresh, audio, persistence, and the full game loop.
- Update the README with web build, development server, artifact, and Pages URLs.
- Document supported browsers, local-storage limitations, and the JavaScript-first/WebAssembly-later
  strategy.
- Add a rollback procedure using the last known-good Pages workflow artifact or commit.

## Acceptance Criteria

- All supported-browser release checks pass against GitHub Pages.
- Known browser limitations are documented rather than implicit.
- A new contributor can build and serve the site from a clean checkout.
- The live JavaScript site is treated as the baseline for later WebAssembly comparisons.

## Verification

- `./gradlew qualityGate webBuild nativeImage`
- Automated smoke tests locally and against the Pages URL.
- Manual supported-browser checklist recorded in the release notes.
