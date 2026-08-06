# WEB-11: Validate and Document the JavaScript Release

Status: complete

Depends on: WEB-10

## Goal

Declare the JavaScript website supported only after production behavior and maintenance procedures
are documented and verified.

## Scope

- Run the release matrix on current Chrome, Firefox, Edge, a current WebKit compatibility build,
  and representative touch input.
- Verify direct navigation, reload, cache refresh, audio, persistence, and the full game loop.
- Update the README with web build, development server, artifact, and Pages URLs.
- Document supported browsers, local-storage limitations, and the JavaScript-first/WebAssembly-later
  strategy.
- Add a rollback procedure using the last known-good Pages workflow artifact or commit.
- Keep real macOS Safari validation visible as a follow-up when that platform is unavailable.

## Acceptance Criteria

- All supported-browser release checks pass against GitHub Pages, with branded Safari remaining
  provisional until WEB-11A passes.
- Known browser limitations are documented rather than implicit.
- A new contributor can build and serve the site from a clean checkout.
- The live JavaScript site is treated as the baseline for later WebAssembly comparisons.

## Verification

- `./gradlew qualityGate webBuild nativeImage`
- Automated smoke tests locally and against the Pages URL.
- Manual supported-browser checklist recorded in the release notes.

## Completion Notes

Completed on 2026-08-03.

- Added a contributor and operator guide covering the clean build, development server, production
  artifact, live Pages URL, support policy, browser data, audio policy, WebAssembly strategy, and
  rollback.
- Passed live full-game release flows in actual Chrome 151 and Edge 151 on Windows, Firefox engine
  151, WebKit 26.5, and Chromium 149 touch emulation.
- Verified cache-busted direct navigation, repository-relative assets, canvas rendering, audio
  resume after interaction, maze editing, game completion, local persistence, and reload without
  page, console, request, or HTTP response errors.
- Kept branded macOS Safari support provisional because that browser was unavailable, and added
  WEB-11A so real Safari validation cannot be lost or bypassed before WebAssembly rollout.
- Built `webBuild` successfully from a clean recursive HTTPS clone, started `webRun`, and confirmed
  its development URL returned the game.
- Passed `spotlessApply qualityGate webBuild nativeImage` under the SDKMAN GraalVM 21 installation,
  including formatting, static analysis, coverage verification, and automated Chromium smoke.
- Qualified the workflow re-run rollback by GitHub's 30-day limit and documented commit revert as
  the durable and older-release path.
- Received approval from both general and simplicity-focused reviewers with no remaining findings.
