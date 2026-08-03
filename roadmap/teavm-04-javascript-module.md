# WEB-04: Add the TeaVM JavaScript Module

Status: complete

Depends on: WEB-03

## Goal

Produce the first playable static JavaScript build from a dedicated browser launcher module.

## Scope

- Replace the WEB-01 compatibility probe with the production `TeaVMLauncher`, using
  `WebApplication`, `MazeGame`, and a full-window canvas.
- Configure only the JavaScript target initially.
- Copy `mouse-sprites.png` and `audio/exploreMaze_T1.mp3` explicitly; do not ship the XCF source.
- Set the title, 1280x720 HTML dimensions, stable storage prefix, development server, source maps
  for development, and optimized/obfuscated release settings.
- Add root `webRun` and `webBuild` convenience tasks.
- Include the new module in repository-wide quality and architecture checks.
- Apply JaCoCo to JVM-testable browser configuration and adapter logic. Exclude only the TeaVM
  `main` entry point and generated code through explicit, documented patterns.

## Acceptance Criteria

- `gdx_teavm_web_js_run` serves a page with a rendered Maze Game canvas.
- `gdx_teavm_web_js_build` creates a self-contained webapp with `index.html` at its root.
- The output contains only runtime assets.
- Core code does not depend on gdx-teavm classes.
- JVM-testable browser logic is included in the repository's 60% line, 40% branch, and 40%
  per-source-file coverage rules.
- Existing desktop and Native Image targets still pass.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew webBuild`
- `./gradlew nativeImage`
- Manual Chrome play-through through the TeaVM development server.

## Out of Scope

- Production Pages deployment.
- WebAssembly output.

## Completion Notes

Completed on 2026-08-03.

- Replaced the isolated toolchain probe with a production `TeaVMLauncher` that starts `MazeGame`
  in a full-window `WebApplication` canvas.
- Added JVM-tested browser backend and runtime configuration factories with a stable
  `maze-game_` storage prefix and browser capability values.
- Added `webBuild` and `webRun`; development runs use source maps and unobfuscated output, while
  release builds use aggressive optimization, obfuscation, and no debug artifacts.
- Staged only `mouse-sprites.png` and `audio/exploreMaze_T1.mp3` from project assets so their
  relative paths are preserved and the XCF source is excluded. Release output is cleaned before
  generation to prevent stale development files.
- Applied JaCoCo to the TeaVM module with only the browser process entry point excluded, and added
  the TeaVM production classes to repository architecture analysis.
- Passed `qualityGate`, optimized `webBuild`, and `nativeImage`.
- Verified the development server in headless Chromium: the 1280 by 720 canvas rendered and
  navigation from the main menu through level selection into Milestone 1 produced no page,
  console, asset, or HTTP errors.
- Received approval from both general and simplicity-focused reviewers with no remaining findings.
