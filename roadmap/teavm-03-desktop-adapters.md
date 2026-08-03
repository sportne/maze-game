# WEB-03: Isolate Desktop Filesystem and Screenshot Behavior

Status: pending

Depends on: WEB-02

## Goal

Remove JVM filesystem APIs from TeaVM-reachable core code while retaining desktop asset discovery
and command-line screenshots.

## Scope

- Reduce the core asset-path type to browser-safe relative asset names.
- Move environment-variable, working-directory, absolute-path, and existence checks into an LWJGL3
  asset resolver.
- Move `ScreenshotCapture`, directory creation, framebuffer PNG writing, and their tests into the
  LWJGL3 adapter boundary.
- Connect the desktop implementations through the runtime configuration introduced by WEB-02.
- Apply coverage verification to JVM-testable LWJGL3 adapter logic. Add narrowly documented JaCoCo
  exclusions only for process entry points and native-image integration classes that cannot run in
  the unit-test JVM; do not exclude path resolution, option parsing, or screenshot coordination.
- Ensure the native-image metadata still includes the default bitmap font and required desktop
  resources.

## Acceptance Criteria

- No main source reachable from the browser imports `java.nio.file` or uses `System.getenv` for
  asset resolution.
- Desktop `--screenshot`, `--screenshot-delay`, and asset-directory behavior is unchanged.
- JVM desktop and Native Image launches still locate both production assets.
- Architecture tests enforce the desktop boundary.
- The LWJGL3 module's testable production logic is included in the 60% line, 40% branch, and 40%
  per-source-file coverage rules.

## Verification

- `./gradlew spotlessApply`
- `./gradlew qualityGate`
- `./gradlew nativeImage`
- Manual LWJGL3 screenshot smoke test with audio disabled.

## Out of Scope

- Browser screenshots or browser download support.
