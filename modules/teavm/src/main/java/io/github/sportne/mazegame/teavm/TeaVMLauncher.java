package io.github.sportne.mazegame.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import io.github.sportne.mazegame.MazeGame;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

/** Launches Maze Game in a gdx-teavm browser application. */
public final class TeaVMLauncher {
  private static boolean pageHiding;
  private static boolean loadingStateHidden;

  private TeaVMLauncher() {}

  /**
   * Starts the JavaScript application.
   *
   * @param args unused TeaVM entry-point arguments
   */
  public static void main(String[] args) {
    configurePage();
    try {
      new WebApplication(
          new MazeGame(
              TeaVMRuntimeConfiguration.create(
                  assetPath -> Gdx.files.internal(assetPath), TeaVMLauncher::gameRendered),
              TeaVMBestResultStore.create()),
          TeaVMApplicationConfiguration.create()) {
        @Override
        protected void onError(Throwable error) {
          showFailureState();
          super.onError(error);
        }
      };
    } catch (RuntimeException | Error error) {
      showFailureState();
      throw error;
    }
  }

  private static void configurePage() {
    HTMLDocument document = HTMLDocument.current();
    installPageHideGuard(document);
    HTMLElement canvas = document.getElementById(TeaVMApplicationConfiguration.CANVAS_ID);
    canvas.addEventListener("contextmenu", event -> event.preventDefault());

    HTMLElement guidance = document.getElementById("viewport-guidance");
    updateGuidance(guidance);
    Window.current().addEventListener("resize", event -> updateGuidance(guidance));
  }

  private static void installPageHideGuard(HTMLDocument document) {
    Window.current().addEventListener("pagehide", event -> pageHiding = true);
    document.addEventListener(
        "visibilitychange",
        event -> {
          if (pageHiding) {
            stopImmediatePropagation(event);
          }
        });
  }

  @JSBody(params = "event", script = "event.stopImmediatePropagation();")
  private static native void stopImmediatePropagation(Event event);

  private static void gameRendered(float ignoredDelta) {
    if (!loadingStateHidden) {
      HTMLDocument.current().getElementById("loading-state").setAttribute("hidden", "");
      loadingStateHidden = true;
    }
  }

  private static void showFailureState() {
    HTMLDocument document = HTMLDocument.current();
    document.getElementById("loading-state").setAttribute("hidden", "");
    document.getElementById("failure-state").removeAttribute("hidden");
  }

  private static void updateGuidance(HTMLElement guidance) {
    Window window = Window.current();
    boolean guidanceRequired =
        BrowserViewportPolicy.requiresGuidance(window.getInnerWidth(), window.getInnerHeight());
    if (guidanceRequired) {
      guidance.removeAttribute("hidden");
    } else {
      guidance.setAttribute("hidden", "");
    }
  }
}
