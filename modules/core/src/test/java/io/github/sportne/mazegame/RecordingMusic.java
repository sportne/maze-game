package io.github.sportne.mazegame;

import com.badlogic.gdx.audio.Music;

/** Test music implementation that records playback and configuration calls. */
public final class RecordingMusic implements Music {
  private boolean disposed;
  private boolean looping;
  private boolean playing;
  private boolean stopped;
  private float volume;

  /** Returns whether dispose was called. */
  public boolean disposed() {
    return disposed;
  }

  /** Returns whether looping was enabled. */
  public boolean looping() {
    return looping;
  }

  /** Returns whether play was called without a later stop. */
  public boolean playing() {
    return playing;
  }

  /** Returns whether stop was called. */
  public boolean stopped() {
    return stopped;
  }

  /** Returns the latest configured volume. */
  public float volume() {
    return volume;
  }

  @Override
  public void play() {
    playing = true;
  }

  @Override
  public void pause() {}

  @Override
  public void stop() {
    stopped = true;
    playing = false;
  }

  @Override
  public boolean isPlaying() {
    return playing;
  }

  @Override
  public void setLooping(boolean isLooping) {
    looping = isLooping;
  }

  @Override
  public boolean isLooping() {
    return looping;
  }

  @Override
  public void setVolume(float volume) {
    this.volume = volume;
  }

  @Override
  public float getVolume() {
    return volume;
  }

  @Override
  public void setPan(float pan, float volume) {
    this.volume = volume;
  }

  @Override
  public void setPosition(float position) {}

  @Override
  public float getPosition() {
    return 0.0F;
  }

  @Override
  public void dispose() {
    disposed = true;
  }

  @Override
  public void setOnCompletionListener(OnCompletionListener listener) {}
}
