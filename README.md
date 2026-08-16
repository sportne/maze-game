# Maze Game

Small Java/libGDX maze game @ https://sportne.github.io/maze-game/.

The supported build baseline is Gradle 9.5.1, Java 21, and libGDX 1.14.2. The playable browser
module uses gdx-teavm 1.6.1, which embeds TeaVM 0.15.0. TeaVM JavaScript generation currently runs
without Gradle's configuration cache because gdx-teavm 1.6.1 captures a non-serializable task
logger.

Maze Game includes ten authored levels, persistent unlock progression and per-level best results,
finite/infinite Wall and Slow Floor inventory, responsive click/drag building, fixed level geometry,
movable preset cells, and four distinct solver behaviors. Levels 1–4 are preset tutorials that
introduce Random, Scout, Tracker, and Seeker with only one or two remaining Wall placements. Level 5
opens the grid completely and introduces Slow Floors beside infinite Walls. Levels 6–9 combine the
known systems on increasingly large single-solver boards, and Level 10 introduces two solvers on a
10x10 grid for the first time. The complete current catalog is specified in the
[level progression design](docs/level-progression-design.md).

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
The [Milestone 3 release baseline](docs/milestone-3-release.md) and earlier
[Milestone 2 baseline](docs/milestone-2-release.md) remain as release history.
The [Milestone 4 roadmap](roadmap/milestone-4.md) defines the inventory-based Wall and Slow Floor
building system. Authored supply, transactional maze inventory, shared solver timing, phase-safe
session editing, responsive click/drag building, placed-cell repositioning, and the balanced fourth
level are implemented; cross-platform release coverage and playtest remain ordered work.

`assets/audio/exploreMaze_T1.mp3` plays as looping background music by default. In environments
without a working OpenAL/PipeWire setup, disable audio with `--no-audio`,
`-DmazeGame.audio=false`, or `MAZE_GAME_AUDIO=false`.

Runtime character art is generated with `./gradlew processSpriteSheets` from the source masters in
`assets`. Random uses the classic mouse artwork with cheese, Scout uses the basic squirrel with an
acorn, and Tracker uses the raccoon with a trash-can goal. Each character displays the front, back,
left, or right frame matching its most recent movement, retaining the right-facing frame before its
first move. The goals come from the processed goal sheet. The checked-in 128-pixel sheets use a
shared frame grid, nearest-neighbor filtering, and an indexed palette of at most 256 colors without
dithering. The quality gate verifies that generated outputs are current.

When run under WSLg, the Gradle JVM and native-image run tasks default OpenAL to WSLg's
PulseAudio socket at `/mnt/wslg/PulseServer`. Gradle also passes `MAZE_GAME_ASSETS_DIR`
so JVM and native runs find the shared `assets` directory from any launch working directory.
