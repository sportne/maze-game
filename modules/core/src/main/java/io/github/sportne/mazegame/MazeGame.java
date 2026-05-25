package io.github.sportne.mazegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.file.Files;
import java.nio.file.Path;

/** Main libGDX application for Maze Game. */
public final class MazeGame extends ApplicationAdapter {
  private static final Color BACKGROUND = new Color(0.07F, 0.08F, 0.10F, 1.0F);
  private static final String ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE = "MAZE_GAME_ASSETS_DIR";
  private static final String BACKGROUND_MUSIC_PATH = "audio/exploreMaze_T1.mp3";
  private static final String PROJECT_BACKGROUND_MUSIC_PATH = "assets/" + BACKGROUND_MUSIC_PATH;
  private static final float BACKGROUND_MUSIC_VOLUME = 0.1F;
  private static final String TITLE = "Maze Game";
  private Music backgroundMusic;

  public MazeGame() {
    this(null);
  }

  MazeGame(Music backgroundMusic) {
    this.backgroundMusic = backgroundMusic;
  }

  /** Returns the display title used by launchers. */
  public static String title() {
    return TITLE;
  }

  static Color background() {
    return new Color(BACKGROUND);
  }

  static String backgroundMusicPath() {
    return BACKGROUND_MUSIC_PATH;
  }

  static String backgroundMusicPath(String assetsDirectory, String userDirectory) {
    if (assetsDirectory != null && !assetsDirectory.isBlank()) {
      return Path.of(assetsDirectory, BACKGROUND_MUSIC_PATH).toString();
    }
    if (Files.exists(Path.of(userDirectory, BACKGROUND_MUSIC_PATH))) {
      return BACKGROUND_MUSIC_PATH;
    }
    return PROJECT_BACKGROUND_MUSIC_PATH;
  }

  static float backgroundMusicVolume() {
    return BACKGROUND_MUSIC_VOLUME;
  }

  static void configureBackgroundMusic(Music music) {
    music.setLooping(true);
    music.setVolume(BACKGROUND_MUSIC_VOLUME);
  }

  @Override
  public void create() {
    backgroundMusic = Gdx.audio.newMusic(backgroundMusicFile());
    configureBackgroundMusic(backgroundMusic);
    backgroundMusic.play();
  }

  @Override
  public void render() {
    ScreenUtils.clear(background());
  }

  @Override
  public void dispose() {
    if (backgroundMusic != null) {
      backgroundMusic.dispose();
      backgroundMusic = null;
    }
  }

  private static FileHandle backgroundMusicFile() {
    String path =
        backgroundMusicPath(
            System.getenv(ASSETS_DIRECTORY_ENVIRONMENT_VARIABLE), System.getProperty("user.dir"));
    if (Path.of(path).isAbsolute()) {
      return Gdx.files.absolute(path);
    }
    return Gdx.files.internal(path);
  }
}
