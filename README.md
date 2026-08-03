# Maze Game

Small Java/libGDX maze game.

The supported build baseline is Gradle 9.5.1, Java 21, and libGDX 1.14.2. The playable browser
module uses gdx-teavm 1.6.1, which embeds TeaVM 0.15.0. TeaVM JavaScript generation currently runs
without Gradle's configuration cache because gdx-teavm 1.6.1 captures a non-serializable task
logger.

Milestone 1 is complete: the desktop app includes the first playable 5x5 level, menu navigation,
level selection scaffold, retry/replay, and persistent best results for completed passing runs.

## Local Commands

- `./gradlew checkAll`
- `./gradlew qualityGate`
- `./gradlew :modules:lwjgl3:run`
- `./gradlew webBuild`
- `./gradlew webRun`
- `./gradlew nativeImage`
- `./gradlew nativeRun`

`webBuild` produces an optimized static site in `modules/teavm/build/dist/js/webapp`; `webRun`
starts the development server with source maps and automatic reload enabled.

`assets/audio/exploreMaze_T1.mp3` plays as looping background music by default. In environments
without a working OpenAL/PipeWire setup, disable audio with `--no-audio`,
`-DmazeGame.audio=false`, or `MAZE_GAME_AUDIO=false`.

When run under WSLg, the Gradle JVM and native-image run tasks default OpenAL to WSLg's
PulseAudio socket at `/mnt/wslg/PulseServer`. Gradle also passes `MAZE_GAME_ASSETS_DIR`
so JVM and native runs find the shared `assets` directory from any launch working directory.
