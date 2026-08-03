package io.github.sportne.mazegame.teavm;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

/** Minimal application used to prove that the pinned TeaVM toolchain can compile libGDX code. */
public final class TeaVMToolchainProbe extends ApplicationAdapter {
  /** Draws a visible frame when the browser backend has started successfully. */
  @Override
  public void render() {
    ScreenUtils.clear(0.07F, 0.08F, 0.10F, 1.0F);
  }
}
