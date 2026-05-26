package io.github.sportne.mazegame.assets;

import com.badlogic.gdx.audio.Music;

/** Coordinates background music configuration, playback, toggling, and disposal. */
public final class BackgroundMusicController {
  /** Quiet default music volume. */
  private static final float BACKGROUND_MUSIC_VOLUME = 0.1F;

  /** Whether the backend audio system is available for this run. */
  private final boolean audioAvailable;

  /** Current music instance, or null when not loaded. */
  private Music backgroundMusic;

  /** Whether session audio is currently enabled. */
  private boolean audioEnabled;

  /**
   * Creates a controller.
   *
   * @param audioAvailable true when audio can be toggled on
   */
  public BackgroundMusicController(boolean audioAvailable) {
    this.audioAvailable = audioAvailable;
    this.audioEnabled = audioAvailable;
  }

  /**
   * Adopts an already-created music instance for later playback and disposal.
   *
   * @param music music instance to own
   */
  public void adopt(Music music) {
    backgroundMusic = music;
  }

  /**
   * Returns the configured startup music volume.
   *
   * @return volume from 0.0 to 1.0
   */
  public static float backgroundMusicVolume() {
    return BACKGROUND_MUSIC_VOLUME;
  }

  /**
   * Applies looping and volume settings to the background music.
   *
   * @param music music instance created by libGDX
   */
  public static void configureBackgroundMusic(Music music) {
    music.setLooping(true);
    music.setVolume(BACKGROUND_MUSIC_VOLUME);
  }

  /**
   * Returns whether session audio is currently enabled.
   *
   * @return true when settings allow music playback
   */
  public boolean audioEnabled() {
    return audioEnabled;
  }

  /**
   * Returns whether backend audio is available.
   *
   * @return true when audio resources can be created or played
   */
  public boolean audioAvailable() {
    return audioAvailable;
  }

  /**
   * Starts playback if audio is enabled and a music instance is available.
   *
   * @param musicLoader loader used to lazily create music
   */
  public void start(MusicLoader musicLoader) {
    if (!audioAvailable || !audioEnabled) {
      return;
    }
    if (backgroundMusic == null) {
      backgroundMusic = musicLoader.load();
    }
    if (backgroundMusic != null) {
      configureBackgroundMusic(backgroundMusic);
      backgroundMusic.play();
    }
  }

  /**
   * Toggles session audio and starts or stops playback as needed.
   *
   * @param musicLoader loader used to lazily create music when toggling on
   */
  public void toggle(MusicLoader musicLoader) {
    if (!audioAvailable) {
      audioEnabled = false;
      return;
    }
    audioEnabled = !audioEnabled;
    if (audioEnabled) {
      start(musicLoader);
    } else if (backgroundMusic != null) {
      backgroundMusic.stop();
    }
  }

  /** Stops and disposes the current music instance when one exists. */
  public void dispose() {
    if (backgroundMusic != null) {
      backgroundMusic.stop();
      backgroundMusic.dispose();
      backgroundMusic = null;
    }
  }

  /** Loader for lazily creating a libGDX music instance. */
  @FunctionalInterface
  public interface MusicLoader {
    /**
     * Loads the background music instance.
     *
     * @return loaded music, or null when unavailable
     */
    Music load();
  }
}
