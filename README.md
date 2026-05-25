# Maze Game

Small Java/libGDX maze game.

## Local Commands

- `./gradlew checkAll`
- `./gradlew qualityGate`
- `./gradlew :modules:lwjgl3:run`
- `./gradlew nativeImage`
- `./gradlew nativeRun`

`assets/audio/exploreMaze_T1.mp3` plays as looping background music by default. In environments
without a working OpenAL/PipeWire setup, disable audio with `--no-audio`,
`-DmazeGame.audio=false`, or `MAZE_GAME_AUDIO=false`.

When run under WSLg, the Gradle JVM and native-image run tasks default OpenAL to WSLg's
PulseAudio socket at `/mnt/wslg/PulseServer`. Gradle also passes `MAZE_GAME_ASSETS_DIR`
so JVM and native runs find the shared `assets` directory from any launch working directory.
