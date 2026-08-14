# Processed sprite sheets

These files are deterministic runtime derivatives of the source masters in the parent directory.
Do not edit the processed PNGs directly. Regenerate them from the project root with:

```shell
./gradlew processSpriteSheets
```

Gradle installs the pinned pngquant 3.0.3 and Oxipng 8.0.0 binaries from the project's npm lockfile;
ImageMagick must be available locally. The script also accepts `PNGQUANT` and `OXIPNG` overrides
when run directly. It discovers the real transparent gutters in each new source rather than
assuming that the source artwork is evenly divided. It then removes the border-connected near-black
background while retaining enclosed black details such as eyes and outlines. Existing transparency
in the original `mouse-sprites.png` source is preserved directly.

Every runtime frame occupies a 128 by 128 pixel cell with an 8 pixel transparent safe inset. All
frames from a source sheet share one scale and are centered on the same source-derived baseline.
The sheets are intended for nearest-neighbor texture filtering. Final output uses no-dither
per-sheet perceptual quantization with at most 256 indexed colors followed by maximum-effort
lossless PNG optimization.

`sprite-sheets.json` records the source, row names, column names, and direction order. A frame at
column `x` and row `y` starts at pixel `(x * 128, y * 128)`.

The game loads `classic-mouse.png`, `basic-characters.png`, and `goals.png`. Browser packaging copies
only those runtime sheets; the remaining processed atlases are ready for future characters without
inflating the current download.

The current size and quality experiments are recorded in the
[sprite asset optimization benchmark](../../docs/sprite-asset-optimization.md).
