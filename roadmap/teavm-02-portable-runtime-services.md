# WEB-02: Introduce Portable Runtime Services

Status: pending

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
