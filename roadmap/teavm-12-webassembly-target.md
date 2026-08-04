# WEB-12: Add the TeaVM WebAssembly Target

Status: complete

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

## Completion Notes

Completed on 2026-08-03.

- Added a gdx-teavm WasmGC target that shares the production launcher and explicit assets while
  writing to `build/dist/wasm/webapp`, independently of the JavaScript release.
- Production builds use aggressive optimization without debug/source output. `webWasmRun` enables
  unobfuscated development output, source maps, copied sources, and the persistent development
  server.
- Copied TeaVM's required `app.wasm-runtime.js` alongside `app.wasm` and installed a small
  WebAssembly-specific bootstrap shell around the existing game canvas and status elements.
- Added `webWasmBuild` and `webWasmRun`, artifact verification, and a Chromium smoke test that
  covers startup, audio loading, pointer input, persistence/reload, viewport guidance, runtime
  assets, and the required `application/wasm` response type.
- Kept GitHub Pages on the JavaScript artifact. CI now builds and smoke-tests both targets and
  uploads the WebAssembly preview as a separate artifact.
- Passed a clean `spotlessApply qualityGate webBuild webWasmBuild nativeImage` under the SDKMAN
  GraalVM 21 installation, including formatting, static analysis, coverage verification, both
  Chromium browser flows, and native-image compilation.
- Received approval from both general and simplicity-focused reviewers after resolving explicit
  cleanup ordering and Web Audio resume coverage findings.
