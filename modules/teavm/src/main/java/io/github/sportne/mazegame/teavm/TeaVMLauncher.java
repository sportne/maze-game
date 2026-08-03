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

  private TeaVMLauncher() {}

  /**
   * Starts the JavaScript application.
   *
   * @param args unused TeaVM entry-point arguments
   */
  public static void main(String[] args) {
    configurePage();
    new WebApplication(
        new MazeGame(
            TeaVMRuntimeConfiguration.create(assetPath -> Gdx.files.internal(assetPath)),
            TeaVMBestResultStore.create()),
        TeaVMApplicationConfiguration.create());
  }

  private static void configurePage() {
    HTMLDocument document = HTMLDocument.current();
    installPageHideGuard(document);
    HTMLElement viewport = document.createElement("meta");
    viewport.setAttribute("name", "viewport");
    viewport.setAttribute("content", "width=device-width, initial-scale=1");
    document.getHead().appendChild(viewport);

    HTMLElement canvas = document.getElementById(TeaVMApplicationConfiguration.CANVAS_ID);
    canvas.getStyle().setProperty("display", "block");
    canvas.getStyle().setProperty("touch-action", "none");
    canvas.addEventListener("contextmenu", event -> event.preventDefault());

    HTMLElement guidance = document.createElement("div");
    guidance.setAttribute("id", "viewport-guidance");
    guidance.setInnerText(
        "Maze Game needs a landscape window at least 640 × 360. Rotate or resize to play.");
    guidance
        .getStyle()
        .setCssText(
            "position:fixed;inset:0;z-index:1;display:flex;align-items:center;"
                + "justify-content:center;padding:24px;box-sizing:border-box;background:#000;"
                + "color:#fff;font:20px sans-serif;text-align:center");
    document.getBody().appendChild(guidance);

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

  private static void updateGuidance(HTMLElement guidance) {
    Window window = Window.current();
    boolean guidanceRequired =
        BrowserViewportPolicy.requiresGuidance(window.getInnerWidth(), window.getInnerHeight());
    guidance.getStyle().setProperty("display", guidanceRequired ? "flex" : "none");
  }
}
