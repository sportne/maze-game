# WEB-01: Align and Validate the TeaVM Toolchain

Status: complete

Depends on: none

## Goal

Establish one supported and reproducible dependency baseline before browser-specific source code is
introduced.

## Scope

- Upgrade libGDX from 1.14.1 to 1.14.2.
- Retain Gradle 9.5.1 unless the real compile probe demonstrates that a wrapper upgrade is required.
- Add gdx-teavm 1.6.1 to the version catalog.
- Add an isolated `modules/teavm` compatibility probe that applies the plugin and compiles a
  minimal libGDX `ApplicationAdapter` to JavaScript without depending on `modules/core`.
- Configure the probe with the same Java 21 toolchain and Gradle quality conventions intended for
  the real browser module.
- Generate and inspect a minimal `index.html` and JavaScript artifact, and serve it once through the
  gdx-teavm development server to validate task creation and runtime startup.
- Remove or update any redundant libGDX version property so the version catalog remains the single
  source of truth.
- Confirm the existing core, LWJGL3, architecture, and native-image builds remain compatible.
- Record the chosen libGDX, gdx-teavm, TeaVM, Gradle, and Java versions in project documentation.

## Acceptance Criteria

- All resolved libGDX modules use version 1.14.2.
- The `com.github.xpenatan.gdx-teavm` plugin marker at 1.6.1 resolves from the configured release
  repositories without snapshot repositories.
- `:modules:teavm:gdx_teavm_web_js_build` performs a real TeaVM compile and generates a runnable
  JavaScript probe artifact.
- `:modules:teavm:gdx_teavm_web_js_run` serves the probe without startup or browser console errors.
- Java remains at release 21.
- The existing desktop launcher tests and Native Image build pass unchanged.
- No dynamic or snapshot dependency is introduced.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew nativeImage`
- `./gradlew :modules:teavm:gdx_teavm_web_js_build`
- `./gradlew :modules:lwjgl3:dependencyInsight --dependency com.badlogicgames.gdx --configuration runtimeClasspath`
- Manual development-server load of the minimal probe.

## Out of Scope

- Connecting the browser module to `MazeGame` or production assets.
- Refactoring shared runtime code.
- Producing the playable web artifact.

## Completion Notes

Completed on 2026-08-03.

- Retained Gradle 9.5.1 after both the JVM build and real TeaVM JavaScript compile succeeded.
- Standardized libGDX on 1.14.2 and pinned gdx-teavm 1.6.1, which resolves TeaVM 0.15.0.
- Added an isolated `modules/teavm` probe with a 320 by 180 browser canvas; it deliberately does
  not depend on `modules/core` or launch the game.
- Disabled configuration-cache reuse only for the affected gdx-teavm tasks because version 1.6.1
  captures a Gradle task logger while configuring TeaVM. The rest of the build retains the existing
  configuration-cache setting.
- Excluded only TeaVM's Java 11 annotation processor from the Java 21 annotation-processor path;
  Error Prone and the project's `-Werror` compilation remain active.
- Verified both the gdx-teavm development server and the generated static site with headless
  Chrome. The WebGL context stayed active, emitted no page errors, and the canvas center pixel
  matched the probe's expected RGBA value `(18, 20, 26, 255)`.
- Passed `qualityGate`, including tests, JaCoCo coverage verification, Checkstyle, SpotBugs, PMD,
  CPD, and Spotless checks.
- Passed `nativeImage` with the installed GraalVM Community Java 21 toolchain.
- Confirmed the LWJGL3 runtime resolves libGDX core, backend, and platform artifacts at 1.14.2.
