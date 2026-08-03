# JavaScript Web Release

The production browser release is the optimized TeaVM JavaScript site at
<https://sportne.github.io/maze-game/>. JavaScript is the compatibility and behavior baseline for
the planned WebAssembly work in WEB-12 and WEB-13.

## Build and Serve

Use Java 21 and clone the PMD submodule with the repository:

```text
git clone --recurse-submodules https://github.com/sportne/maze-game.git
cd maze-game
./gradlew webBuild
```

The full quality gate also requires Node.js with npm. On a clean Linux browser-test host, install
Playwright's system libraries once with
`./gradlew :modules:browser-tests:playwrightInstallDependencies`.

The release artifact is `modules/teavm/build/dist/js/webapp`. Preview a development build with
source maps and automatic reload at <http://localhost:8080>:

```text
./gradlew webRun
```

Run `./gradlew qualityGate webBuild` before reviewing or publishing a release. GitHub Pages builds
the same artifact from source and deploys only after that command passes.

## Browser Support

The supported desktop baseline is the current stable Chrome, Edge, and Firefox releases with
JavaScript, WebGL, Web Audio, and local storage enabled. Touch input is supported on a landscape
viewport at least 640 by 360 CSS pixels. Smaller or portrait viewports receive resize guidance.

The release matrix was run against the live Pages URL on 2026-08-03:

| Browser or input | Version exercised | Result |
| --- | --- | --- |
| Google Chrome on Windows | 151.0.7922.71 | Full mouse game flow passed |
| Microsoft Edge on Windows | 151.0.4129.59 | Full mouse game flow passed |
| Firefox engine | 151.0 | Full mouse game flow passed |
| WebKit compatibility | 26.5 | Full mouse game flow passed |
| Safari on macOS | 26.5.2 | Automated branded Safari release flow passed |
| Chromium touch emulation | 149.0.7827.55 | Full touch game flow passed |

Each passing flow covered cache-busted direct navigation, relative asset loading from
`/maze-game/`, canvas initialization, audio loading and Web Audio resume after interaction, menu
and level selection, maze editing, game completion, local persistence, and reload. The Chrome,
Edge, Firefox, WebKit, and touch-emulation flows observed no page, console, request, or HTTP
response errors.

The Pages workflow validates every deployment in branded Safari through Apple's `safaridriver` on
GitHub's `macos-15` runner. The test records the exact browser and platform version with its
screenshot artifact. It covers the live game loop, required asset HTTP status and MIME types,
audio loading, runtime errors after initialization, local persistence, and refresh behavior.
SafariDriver does not expose complete browser-console history, so initialization is additionally
guarded by the page's visible failure state. Real iPhone or iPad Safari has not been exercised and
remains an explicit rollout constraint.

## Browser Data and Audio

Best results use browser local storage. They are scoped to the Pages origin and browser profile;
they do not synchronize across browsers or devices and can disappear when site data is cleared,
private browsing ends, or storage is evicted. A storage failure must not prevent the game from
running, but the best result may not survive a reload.

Browsers may keep Web Audio suspended until the first click or touch. The release flow confirms
that audio loads and the context resumes after interaction. Browser mute settings, autoplay
policy, operating-system audio configuration, or accessibility preferences can still prevent
audible output.

## JavaScript and WebAssembly

JavaScript remains the production default. The WebAssembly target will be built and verified in
parallel before any rollout decision. It must match this release's game behavior and browser matrix
and demonstrate a measured benefit; unsupported WebAssembly clients must retain the JavaScript
experience.

## Rollback

For an urgent rollback within GitHub's 30-day workflow re-run window, open the `Deploy GitHub
Pages` workflow in GitHub Actions, select the last known-good run, and re-run all jobs. This
rebuilds and redeploys that run's commit through the same quality and browser gates.

Make every rollback durable by reverting the faulty commit on `main` and pushing the revert; use
this path immediately when the prior run is too old or cannot be re-run. Do not force-push `main`
or hand-edit the deployed artifact. Confirm the replacement workflow succeeds, then repeat direct
navigation, asset, full game, persistence, reload, and audio checks against the live URL.
