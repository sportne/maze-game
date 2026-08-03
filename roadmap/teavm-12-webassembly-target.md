# WEB-12: Add the TeaVM WebAssembly Target

Status: pending

Depends on: WEB-11

## Goal

Produce a WebAssembly version of the same browser application without disrupting the released
JavaScript site.

## Scope

- Add the gdx-teavm `wasm` target using the same launcher and explicit runtime assets.
- Keep JavaScript and WebAssembly output directories independent.
- Copy the required TeaVM WebAssembly runtime alongside the `.wasm` file.
- Configure appropriate production optimization and development source/debug information.
- Add `webWasmBuild` and `webWasmRun` convenience tasks.
- Extend CI to compile and smoke-test WebAssembly without deploying it as the default site.

## Acceptance Criteria

- The WebAssembly build completes from a clean checkout.
- Its full game loop, audio, input, persistence, and resize behavior match JavaScript.
- The static server returns the correct MIME type for `.wasm`.
- JavaScript remains the GitHub Pages default while evaluation is incomplete.
- Existing quality, desktop, native, and JavaScript gates continue to pass.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate webBuild webWasmBuild nativeImage`
- Browser smoke tests for both JavaScript and WebAssembly targets.
