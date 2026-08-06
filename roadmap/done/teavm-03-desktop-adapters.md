# WEB-03: Isolate Desktop Filesystem and Screenshot Behavior

Status: complete

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

## Completion Notes

Completed on 2026-08-03.

- Reduced shared asset paths to browser-safe relative names and moved environment, working
  directory, existence, and absolute-path handling into `DesktopAssetResolver`.
- Moved delayed screenshot coordination, directory creation, framebuffer reading, and PNG writing
  into the LWJGL3 module and connected it through the portable after-render hook.
- Updated the desktop launcher to inject its resolver, screenshot hook, exit behavior, and audio
  capability through `MazeGameRuntimeConfiguration`.
- Applied the standard JaCoCo thresholds to testable LWJGL3 code; only the GraalVM registration
  package is excluded because it is exercised by the Native Image build rather than the test JVM.
- Added architecture guards against NIO filesystem access, `java.io.File`, environment-variable
  reads, and system-property reads outside the desktop adapter boundary.
- Passed `qualityGate`, `nativeImage`, and a real 640 by 360 LWJGL3 framebuffer screenshot smoke
  test with audio disabled. The Native Image contains the default bitmap font resource names and
  both production asset names.
- Received approval from both general and simplicity-focused reviewers after addressing the
  architecture review finding.
