# Maze Game

Small Java/libGDX maze game.

The supported build baseline is Gradle 9.5.1, Java 21, and libGDX 1.14.2. Browser work uses
gdx-teavm 1.6.1, which embeds TeaVM 0.15.0. The initial browser module currently contains an
isolated compatibility probe; it is not connected to the playable game yet. TeaVM JavaScript
generation currently runs without Gradle's configuration cache because gdx-teavm 1.6.1 captures a
non-serializable task logger.

Milestone 1 is complete: the desktop app includes the first playable 5x5 level, menu navigation,
level selection scaffold, retry/replay, and persistent best results for completed passing runs.

## Local Commands

- `./gradlew checkAll`
- `./gradlew qualityGate`
- `./gradlew :modules:lwjgl3:run`
- `./gradlew :modules:teavm:gdx_teavm_web_js_build`
- `./gradlew :modules:teavm:gdx_teavm_web_js_run`
- `./gradlew nativeImage`
- `./gradlew nativeRun`

`assets/audio/exploreMaze_T1.mp3` plays as looping background music by default. In environments
without a working OpenAL/PipeWire setup, disable audio with `--no-audio`,
`-DmazeGame.audio=false`, or `MAZE_GAME_AUDIO=false`.

When run under WSLg, the Gradle JVM and native-image run tasks default OpenAL to WSLg's
PulseAudio socket at `/mnt/wslg/PulseServer`. Gradle also passes `MAZE_GAME_ASSETS_DIR`
so JVM and native runs find the shared `assets` directory from any launch working directory.
