package io.github.sportne.mazegame.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sportne.mazegame.RecordingMusic;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

final class BackgroundMusicControllerTest {
  @Test
  void configuresQuietLoopingPlayback() {
    RecordingMusic music = new RecordingMusic();

    BackgroundMusicController.configureBackgroundMusic(music);

    assertTrue(music.looping());
    assertEquals(0.1F, music.volume());
  }

  @Test
  void startsAvailableEnabledMusicLazily() {
    RecordingMusic music = new RecordingMusic();
    BackgroundMusicController controller = new BackgroundMusicController(true);

    controller.start(() -> music);

    assertTrue(controller.audioEnabled());
    assertTrue(controller.audioAvailable());
    assertTrue(music.playing());
    assertTrue(music.looping());
    assertEquals(0.1F, music.volume());
  }

  @Test
  void unavailableAudioStartsOffAndIgnoresToggle() {
    RecordingMusic music = new RecordingMusic();
    BackgroundMusicController controller = new BackgroundMusicController(false);

    controller.toggle(() -> music);

    assertFalse(controller.audioEnabled());
    assertFalse(music.playing());
  }

  @Test
  void unavailableAudioDoesNotLoadOnStart() {
    AtomicBoolean loaded = new AtomicBoolean(false);
    BackgroundMusicController controller = new BackgroundMusicController(false);

    controller.start(
        () -> {
          loaded.set(true);
          return new RecordingMusic();
        });

    assertFalse(loaded.get());
  }

  @Test
  void adoptedMusicIsDisposedEvenWhenAudioIsUnavailable() {
    RecordingMusic music = new RecordingMusic();
    BackgroundMusicController controller = new BackgroundMusicController(false);

    controller.adopt(music);
    controller.dispose();

    assertTrue(music.stopped());
    assertTrue(music.disposed());
  }

  @Test
  void adoptDoesNotStartPlayback() {
    RecordingMusic music = new RecordingMusic();
    BackgroundMusicController controller = new BackgroundMusicController(true);

    controller.adopt(music);

    assertFalse(music.playing());
    assertTrue(controller.audioEnabled());
  }

  @Test
  void toggleStopsAndRestartsMusic() {
    RecordingMusic music = new RecordingMusic();
    BackgroundMusicController controller = new BackgroundMusicController(true);

    controller.start(() -> music);

    controller.toggle(() -> music);
    assertFalse(controller.audioEnabled());
    assertTrue(music.stopped());

    controller.toggle(() -> music);
    assertTrue(controller.audioEnabled());
    assertTrue(music.playing());
  }

  @Test
  void disposeStopsAndDisposesMusic() {
    RecordingMusic music = new RecordingMusic();
    BackgroundMusicController controller = new BackgroundMusicController(true);

    controller.start(() -> music);

    controller.dispose();

    assertTrue(music.stopped());
    assertTrue(music.disposed());
  }

  @Test
  void backgroundMusicVolumeIsComfortableForStartup() {
    assertEquals(0.1F, BackgroundMusicController.backgroundMusicVolume());
  }
}
