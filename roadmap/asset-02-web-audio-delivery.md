# ASSET-02: Optimize and Stream Web Music

Status: proposed

Depends on: selection of a production background track and availability of its lossless source
master

## Goal

Reduce first-play transfer, decode memory, and startup delay for browser music without an audible
quality regression or unreliable looping.

## Scope

- Create a seamless 60-to-90-second production loop from a lossless source master; do not use one
  of the candidate lossy MP3 files as a new encoding master.
- Benchmark stripped 64 and 48 kbps MP3 plus 40 and 48 kbps Opus/WebM derivatives using the same
  loop boundaries and loudness.
- Add web-specific preferred-source selection with a broadly compatible MP3 fallback instead of
  changing the shared desktop asset to a browser-only codec.
- Choose Web Audio buffering for a short seamless loop or HTML5 Audio streaming for a retained long
  track based on measured startup latency, decoded memory, loop quality, and Safari behavior.
- Request music only after the browser's required user gesture and never download fallback or
  unselected tracks eagerly.
- Give released music a versioned cache identity and a separate compressed-transfer budget.
- Keep rejected candidates, Windows download sidecars, and intermediate encodes out of runtime
  artifacts and version control.

## Acceptance Criteria

- The preferred web music response is no larger than 750 KB, excluding a compatibility fallback
  that is not requested by compatible browsers.
- Loading the main menu without interacting requests no music; the first audio-enabling gesture
  requests exactly one compatible source.
- Chromium and branded Safari play, pause, resume, mute, and loop the track in both JavaScript and
  WebAssembly releases without console errors or an audible loop gap in the accepted test sample.
- A browser that rejects the preferred codec requests the MP3 fallback once and still reaches
  playback without blocking gameplay.
- Desktop keeps supported local playback, and JavaScript, WebAssembly, Pages, and native artifacts
  contain no unselected audio source or derivative.

## Verification

- Record source duration, channels, sample rate, codec, effective bitrate, metadata bytes, encoded
  bytes, and a listening decision for every benchmark variant.
- Capture exact browser request order and transferred bytes before and after the first audio gesture.
- Exercise preferred-codec success, unsupported-codec fallback, failed download, pause/resume,
  mute/unmute, and repeated loop boundaries.
- Run formatting, static analysis, coverage, architecture, browser smoke, artifact-budget, Pages,
  Safari, and native packaging gates before independent review and commit.
