# WEB-04: Add the TeaVM JavaScript Module

Status: pending

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
