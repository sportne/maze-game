# WebAssembly Rollout

## Decision

Maze Game keeps JavaScript as the production default at
<https://sportne.github.io/maze-game/> and publishes WebAssembly as an opt-in preview at
<https://sportne.github.io/maze-game/wasm/>. WebAssembly does not replace the default because the
measured gains are small, its artifact and first-load heap are larger, and its complete real-browser
matrix is not yet as broad as JavaScript's.

Browsers that support gdx-teavm's baseline Wasm helper but not TeaVM's WasmGC `js-string` feature
are redirected from the preview to the independent JavaScript root. The JavaScript CI flow disables
that WasmGC compilation feature, confirms the redirect, and then completes gameplay, audio resume,
persistence, reload, and resize checks. A browser with no core WebAssembly support cannot run either
gdx-teavm target and is outside the documented baseline.

## Baseline Measurements

Measurements were recorded on 2026-08-03 against the production builds through the final Pages
path structure on loopback HTTP. Each startup result is the median of five clean navigations or
reloads. Frame pacing uses 120 consecutive animation-frame intervals after startup. Heap is the Chromium
`performance.memory.usedJSHeapSize` value after the first rendered frame; it is a useful relative
Chromium signal, not total process memory.

| Browser and target | Response end median | First frame median / p95 | Frame interval p95 | Used JS heap |
| --- | ---: | ---: | ---: | ---: |
| Chrome 151 JavaScript | 20.8 ms | 3,756 / 3,886 ms | 17.2 ms | 19,940,667 B |
| Chrome 151 WebAssembly | 11.5 ms | 3,670 / 3,796 ms | 17.4 ms | 22,829,570 B |
| Edge 151 JavaScript | 18.6 ms | 3,880 / 4,084 ms | 18.1 ms | 20,256,127 B |
| Edge 151 WebAssembly | 11.3 ms | 3,839 / 3,918 ms | 18.1 ms | 22,686,064 B |

| Production artifact | Uncompressed | Gzip total |
| --- | ---: | ---: |
| JavaScript | 4,828,560 B | 3,938,862 B |
| WebAssembly | 4,948,309 B | 4,024,798 B |
| WebAssembly difference | +119,749 B (+2.5%) | +85,936 B (+2.2%) |

The audio file dominates both totals. Within that shared payload, WebAssembly showed only a 1–2%
first-frame improvement in the branded Chromium browsers, indistinguishable frame pacing, and a
12–15% larger first-load JS heap signal. This is not a compelling default-switch benefit.

Every CI run retains fresh `metrics.properties` files for both targets in the
`browser-rollout-metrics` artifact. These are diagnostic regression signals rather than hard
performance budgets because shared hosted-runner load affects absolute timing.

## Compatibility Matrix

| Browser or input | WebAssembly result | Notes |
| --- | --- | --- |
| Google Chrome 151 on Windows | Full flow passed | Branded browser through CDP; metrics recorded |
| Microsoft Edge 151 on Windows | Full flow passed | Branded browser through CDP; metrics recorded |
| Chromium 149 touch emulation | Three-level flow passed | Used real touch events in portrait and constrained landscape, not mouse clicks |
| Firefox 151 on Windows | Preview remains provisional | JavaScript passed; local Playwright Firefox lacked a WebGL context for a valid comparison |
| Safari 26.5.2 on macOS | Deployment gate | Pages runs both root and preview flows in branded Safari after every deployment |
| iPhone or iPad Safari | Not verified | Remains an explicit preview constraint |

The full flow covers all three authored levels, locked selection and unlock, audio loading and
gesture resume, maze editing, completion, per-level local persistence, retry, replay, build-screen
back navigation, reload, responsive touch layouts, runtime/asset requests, and errors. The
local server and Safari deployment gate require `application/wasm` for the module response.
Firefox, WebKit compatibility, and real iPhone/iPad Safari must pass this flow before WebAssembly can
be considered for promotion from preview to the default release.

## Hosting, Debugging, and Rollback

`./gradlew pagesBuild` stages one atomic artifact at `modules/teavm/build/dist/pages`: JavaScript at
the root and WebAssembly under `wasm/`. GitHub Pages deploys that directory in one operation, so the
two entry points cannot come from different commits. Both use relative URLs and the same origin;
best results therefore remain available when a player switches entry points.

`webWasmRun` retains source maps, copied sources, and unobfuscated development output. Browser
developer tools are the supported Wasm debugging path; TeaVM's IntelliJ debugger remains specific
to JavaScript. Production builds remove source maps and copied sources.

Rollback remains commit-based and atomic. Re-run the last known-good Pages workflow within GitHub's
retention window, then make the rollback durable by reverting the faulty commit on `main`. Removing
the preview later requires only removing its staging input; the JavaScript root is independent.
