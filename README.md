# Maze Game

Small Java/libGDX maze game.

The supported build baseline is Gradle 9.5.1, Java 21, and libGDX 1.14.2. The playable browser
module uses gdx-teavm 1.6.1, which embeds TeaVM 0.15.0. TeaVM JavaScript generation currently runs
without Gradle's configuration cache because gdx-teavm 1.6.1 captures a non-serializable task
logger.

Milestone 2 is complete with an introductory 5x5 level and a harder
7x7 level, persistent unlock progression and per-level best results, responsive mouse and touch
controls, and retry/replay support.

## Local Commands

- `./gradlew checkAll`
- `./gradlew qualityGate`
- `./gradlew :modules:lwjgl3:run`
- `./gradlew webBuild`
- `./gradlew webRun`
- `./gradlew webWasmBuild`
- `./gradlew webWasmRun`
- `./gradlew nativeImage`
- `./gradlew nativeRun`

`webBuild` produces an optimized static site in `modules/teavm/build/dist/js/webapp`; `webRun`
starts the development server with source maps and automatic reload enabled.

`webWasmBuild` produces an independent WebAssembly preview in
`modules/teavm/build/dist/wasm/webapp`; `webWasmRun` starts its development server. The JavaScript
site remains the GitHub Pages default, and `pagesBuild` stages both targets atomically with the
preview under `wasm/`.

## GitHub Pages

The `Deploy GitHub Pages` workflow builds and verifies the static site from source before deploying
the `main` branch to `https://sportne.github.io/maze-game/`. To enable it for the first time, open
the repository's **Settings > Pages** page and select **GitHub Actions** as the source under **Build
and deployment**, then run the workflow manually once before relying on automatic deployments.
The assembled site retains `.nojekyll` for compatibility with branch-based Pages publishing;
GitHub's Actions-based Pages artifact uploader omits dotfiles and deploys the static files directly
without a Jekyll build.

See [the JavaScript release guide](docs/javascript-release.md) for the browser support matrix,
browser-storage limitations, JavaScript/WebAssembly strategy, and rollback procedure.
The [WebAssembly rollout decision](docs/webassembly-rollout.md) records the preview URL, comparison
metrics, compatibility evidence, constraints, and regression baseline.
The [Milestone 2 release baseline](docs/milestone-2-release.md) records the accepted levels,
playtest evidence, mobile behavior, and known constraints.

`assets/audio/exploreMaze_T1.mp3` plays as looping background music by default. In environments
without a working OpenAL/PipeWire setup, disable audio with `--no-audio`,
`-DmazeGame.audio=false`, or `MAZE_GAME_AUDIO=false`.

When run under WSLg, the Gradle JVM and native-image run tasks default OpenAL to WSLg's
PulseAudio socket at `/mnt/wslg/PulseServer`. Gradle also passes `MAZE_GAME_ASSETS_DIR`
so JVM and native runs find the shared `assets` directory from any launch working directory.
