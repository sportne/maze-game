# WEB-13: Evaluate and Roll Out WebAssembly

Status: complete

Depends on: WEB-11A, WEB-12

## Goal

Use measured compatibility and performance to decide whether WebAssembly should replace,
supplement, or remain secondary to the JavaScript build.

## Scope

- Compare compressed size, startup time, first rendered frame, frame pacing, and memory use.
- Exercise the JavaScript release's supported-browser and touch-input matrix where the available
  environment can produce valid results; record unverified engines as blockers to default
  promotion.
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
- Run the automated smoke suite, branded Chromium browsers, touch input, and the branded Safari
  deployment gate; record any matrix target blocked by the test environment.
- Confirm fallback behavior by deliberately disabling WebAssembly.

## Completion Notes

Completed on 2026-08-03.

- Selected JavaScript default with a WebAssembly preview at `/maze-game/wasm/`; preview startup
  failures redirect to the independent JavaScript root.
- Added `pagesBuild` to stage both verified targets into one atomic Pages artifact and changed both
  CI and deployment to consume that exact directory.
- Ran both targets through their final root and preview path structure. CI disables TeaVM's required
  WasmGC compilation feature, verifies the preview fallback, and proves clients without WasmGC
  retain the full game. Both gdx-teavm targets still require baseline core WebAssembly.
- Retained five-sample startup, first-frame, 120-frame pacing, artifact/gzip size, and Chromium heap
  measurements as CI artifacts and documented the branded Chrome 151 and Edge 151 baselines.
- Passed full mouse flows in branded Chrome and Edge, plus the WebAssembly touch flow in Edge. Kept
  Firefox, WebKit compatibility, and real iPhone/iPad Safari explicitly provisional because the
  local Firefox/WebKit environments could not provide a valid WebGL result. Those targets block
  promotion from preview rather than expanding the support claim beyond the available evidence.
- Extended the branded macOS Safari deployment gate to run the full JavaScript and WebAssembly
  flows, including assets, MIME, audio, persistence, refresh, and runtime error checks.
- Documented Wasm debugging constraints, same-origin persistence, atomic hosting/cache behavior,
  preview removal, and commit-based rollback.
- Passed `spotlessApply qualityGate pagesBuild nativeImage` under the SDKMAN GraalVM 21
  installation, including analysis, coverage, unit, architecture, browser, and native-image checks.
- Received approval from both general and simplicity-focused reviewers after correcting the atomic
  release guide and formally recording the preview's browser-matrix promotion blockers.
