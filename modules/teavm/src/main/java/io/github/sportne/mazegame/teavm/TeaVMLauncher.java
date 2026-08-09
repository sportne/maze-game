package io.github.sportne.mazegame.teavm;

import com.badlogic.gdx.Gdx;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import io.github.sportne.mazegame.MazeGame;
import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.Touch;
import org.teavm.jso.dom.events.TouchEvent;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

/** Launches Maze Game in a gdx-teavm browser application. */
public final class TeaVMLauncher {
  private static boolean pageHiding;
  private static boolean loadingStateHidden;
  private static MazeGame activeGame;
  private static final BrowserTouchGestureOwner TOUCH_GESTURE_OWNER =
      new BrowserTouchGestureOwner();
  private static boolean gestureActiveBeforeTouchStart;

  private TeaVMLauncher() {}

  /**
   * Starts the JavaScript application.
   *
   * @param args unused TeaVM entry-point arguments
   */
  public static void main(String[] args) {
    configurePage();
    try {
      activeGame =
          new MazeGame(
              TeaVMRuntimeConfiguration.create(
                  assetPath -> Gdx.files.internal(assetPath), TeaVMLauncher::gameRendered),
              TeaVMBestResultStore.create());
      new WebApplication(activeGame, TeaVMApplicationConfiguration.create()) {
        @Override
        protected void onError(Throwable error) {
          showFailureState();
          super.onError(error);
        }
      };
      installTouchOwnershipTracking();
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
    canvas.addEventListener(
        "touchstart",
        event ->
            gestureActiveBeforeTouchStart =
                activeGame != null && activeGame.buildGestureState().isPresent(),
        true);
    canvas.addEventListener("touchcancel", TeaVMLauncher::cancelOwningTouch, true);

    HTMLElement guidance = document.getElementById("viewport-guidance");
    updateGuidance(guidance);
    Window.current()
        .addEventListener(
            "resize",
            event -> {
              cancelActiveGesture();
              updateGuidance(guidance);
            });
    Window.current().addEventListener("blur", event -> cancelActiveGesture());
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

  private static void cancelActiveGesture() {
    TOUCH_GESTURE_OWNER.clearOwnership();
    if (activeGame != null) {
      activeGame.cancelBuildGesture();
    }
  }

  private static void installTouchOwnershipTracking() {
    HTMLElement canvas =
        HTMLDocument.current().getElementById(TeaVMApplicationConfiguration.CANVAS_ID);
    canvas.addEventListener("touchstart", TeaVMLauncher::claimStartedTouch, true);
    canvas.addEventListener("touchend", TeaVMLauncher::releaseEndedTouch, true);
  }

  private static void claimStartedTouch(Event event) {
    JSArrayReader<Touch> changedTouches = ((TouchEvent) event).getChangedTouches();
    for (int index = 0; index < changedTouches.getLength(); index++) {
      TOUCH_GESTURE_OWNER.touchStarted(changedTouches.get(index).getIdentifier());
    }
    if (activeGame != null && !gestureActiveBeforeTouchStart) {
      activeGame
          .buildGestureState()
          .ifPresent(state -> TOUCH_GESTURE_OWNER.claimPointer(state.pointerId()));
    }
    gestureActiveBeforeTouchStart = false;
  }

  private static void releaseEndedTouch(Event event) {
    releaseChangedTouches((TouchEvent) event, false);
  }

  private static void cancelOwningTouch(Event event) {
    releaseChangedTouches((TouchEvent) event, true);
  }

  private static void releaseChangedTouches(TouchEvent event, boolean cancelOwner) {
    JSArrayReader<Touch> changedTouches = event.getChangedTouches();
    boolean ownerReleased = false;
    for (int index = 0; index < changedTouches.getLength(); index++) {
      ownerReleased |= TOUCH_GESTURE_OWNER.release(changedTouches.get(index).getIdentifier());
    }
    if (ownerReleased && cancelOwner) {
      cancelActiveGesture();
    }
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
