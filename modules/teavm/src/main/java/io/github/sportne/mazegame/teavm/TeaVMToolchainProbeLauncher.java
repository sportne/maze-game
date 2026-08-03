package io.github.sportne.mazegame.teavm;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;

/** Launches the isolated TeaVM compatibility probe in a browser canvas. */
public final class TeaVMToolchainProbeLauncher {
  /** Prevents instantiation of this process entry point. */
  private TeaVMToolchainProbeLauncher() {}

  /**
   * Starts the probe application.
   *
   * @param args unused TeaVM entry-point arguments
   */
  public static void main(String[] args) {
    WebApplicationConfiguration configuration = new WebApplicationConfiguration("canvas");
    configuration.width = 320;
    configuration.height = 180;
    configuration.preserveDrawingBuffer = true;
    new WebApplication(new TeaVMToolchainProbe(), configuration);
  }
}
