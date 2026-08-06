# WEB-02: Introduce Portable Runtime Services

Status: complete

Depends on: WEB-01

## Goal

Give `MazeGame` explicit platform boundaries so desktop and browser launchers can supply different
runtime behavior without placing TeaVM-specific code in `modules/core`.

## Scope

- Add a small immutable runtime configuration accepted by `MazeGame`.
- Define narrow interfaces for asset resolution and an optional after-render hook.
- Move exit behavior and platform capabilities, including whether Quit is available and whether
  audio requires a user gesture, into the runtime configuration.
- Preserve convenient default constructors for unit tests and normal desktop use.
- Update unit tests for defaults, injected services, and null/invalid configuration.
- Extend architecture tests so `core` cannot depend on either LWJGL3 or gdx-teavm backend classes.

## Acceptance Criteria

- `MazeGame` has no direct dependency on a concrete desktop or web backend.
- Platform services are small enough to fake in core unit tests.
- Existing game-state and rendering behavior remains unchanged.
- New production source files meet the configured per-source-file coverage threshold.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew nativeImage`

## Out of Scope

- Removing all desktop filesystem code; WEB-03 performs that migration.
- Adding the web launcher.

## Completion Notes

Completed on 2026-08-03.

- Added immutable runtime configuration with narrow asset-resolution and after-render boundaries,
  an exit action, and explicit Quit/audio capability values.
- Routed `MazeGame` asset lookup, exit handling, audio availability, and frame-completion work
  through the injected configuration while preserving the existing desktop constructors and
  behavior.
- Kept browser-specific Quit visibility and gesture-gated audio behavior deferred to WEB-06.
- Generalized the existing desktop asset fallback helper so the default resolver retains explicit
  assets-directory, assets working-directory, and project-directory behavior.
- Extended architecture verification to reject both LWJGL3 and gdx-teavm backend dependencies from
  shared code.
- Added tests for defaults, injected services, null validation, and `MazeGame` after-render
  delegation with the exact frame delta.
- Passed `qualityGate`, including tests, JaCoCo coverage verification, Checkstyle, SpotBugs, PMD,
  CPD, Error Prone compilation, and Spotless checks.
- Passed `nativeImage` with the installed GraalVM Community Java 21 toolchain.
- Received approval from both a general implementation reviewer and a simplicity-focused reviewer
  after addressing their findings.
