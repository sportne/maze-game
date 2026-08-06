# TeaVM and GitHub Pages Roadmap

Status: complete

## Goal

Publish Maze Game as a static website on GitHub Pages while retaining the existing JVM desktop
and GraalVM Native Image targets. Deliver JavaScript first, then add and evaluate TeaVM's
WebAssembly output as a follow-on milestone.

## Delivery Decisions

- Use the `com.github.xpenatan.gdx-teavm` Gradle plugin rather than a custom TeaVM builder.
- Align the initial browser toolchain on libGDX 1.14.2, gdx-teavm 1.6.1, and TeaVM 0.15.0.
- Ship JavaScript as the first production browser target.
- Treat WebAssembly as a committed follow-on milestone, not as a blocker for the JavaScript site.
- Deploy generated output with GitHub's Pages artifact workflow from the `main` branch.
- Do not commit generated website output to a `gh-pages` branch.
- Keep best results browser-local; no server-side account or synchronization system is planned.

## Task Order

1. [WEB-01: Align the TeaVM toolchain](done/teavm-01-toolchain-alignment.md)
2. [WEB-02: Introduce portable runtime services](done/teavm-02-portable-runtime-services.md)
3. [WEB-03: Isolate desktop filesystem and screenshot behavior](done/teavm-03-desktop-adapters.md)
4. [WEB-04: Add the JavaScript browser module](done/teavm-04-javascript-module.md)
5. [WEB-05: Make layout and input browser-ready](done/teavm-05-browser-input-layout.md)
6. [WEB-06: Harden browser audio, lifecycle, and persistence](done/teavm-06-browser-runtime-behavior.md)
7. [WEB-07: Add automated browser smoke coverage](done/teavm-07-browser-smoke-tests.md)
8. [WEB-08: Produce a release-quality static site](done/teavm-08-production-webapp.md)
9. [WEB-09: Add continuous-integration web verification](done/teavm-09-ci-web-verification.md)
10. [WEB-10: Deploy the JavaScript site to GitHub Pages](done/teavm-10-github-pages-deployment.md)
11. [WEB-11: Validate and document the JavaScript release](done/teavm-11-javascript-release.md)
12. [WEB-11A: Validate the JavaScript release in Safari](done/teavm-11a-safari-validation.md)
13. [WEB-12: Add the WebAssembly build target](done/teavm-12-webassembly-target.md)
14. [WEB-13: Evaluate and roll out WebAssembly](done/teavm-13-webassembly-rollout.md)

## Required Quality Gate

Every implementation card must preserve the repository's baseline requirements:

- Google Java Format and the configured Gradle/misc formatting rules pass through Spotless.
- Checkstyle, SpotBugs, Error Prone, PMD, and CPD report no violations.
- JaCoCo retains at least 60% bundle line coverage, 40% bundle branch coverage, and 40%
  source-file line coverage.
- Those thresholds apply to JVM-testable production logic in every module, not only
  `modules/core`. Platform entry points, generated sources, and native integration glue may be
  excluded only through narrow, documented patterns that do not exclude application logic.
- Java-based browser automation remains under the Java quality conventions. If JavaScript or
  TypeScript is introduced, pinned formatting and linting tools must be added to `qualityGate`.
- Maintained HTML and CSS are formatted and statically checked by pinned tools wired into
  `qualityGate`.
- Existing desktop and native-image behavior is reverified whenever shared runtime code or libGDX
  versions change.
- Browser-producing cards build the optimized TeaVM target and test the generated files through an
  HTTP server rather than `file://`.

The normal local quality command is `./gradlew qualityGate`. The build must be updated as modules
and source types are added so this command continues to enforce the complete repository rather than
silently skipping them. Cards list additional focused commands where their risk requires them.

## External References

- gdx-teavm compatibility and plugin usage:
  <https://github.com/xpenatan/gdx-teavm/tree/1.6.1>
- gdx-teavm Gradle plugin guide:
  <https://github.com/xpenatan/gdx-teavm/blob/1.6.1/docs/usage.md>
- GitHub Pages custom workflow documentation:
  <https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages>
