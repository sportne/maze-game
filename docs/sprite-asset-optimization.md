# Sprite Asset Optimization Benchmark

Date: 2026-08-13

## Decision Constraints

- Runtime derivatives use at most 256 indexed colors per sheet.
- Palette conversion does not use dithering.
- Source masters remain unchanged.
- The benchmark covers `advanced-characters.png`, `advanced-mice.png`,
  `basic-characters.png`, `classic-mouse.png`, and `goals.png` together.
- The production renderer uses nearest-neighbor texture filtering.

## Tools and Method

- ImageMagick 6.9.12-98 provided the existing palette baseline.
- pngquant 3.0.3 ran with 256 colors, `--nofs`, `--speed 1`, and its maximum quality target.
- Oxipng 10.2.0 ran single-threaded with safe metadata stripping at levels 4, maximum, and maximum
  with Zopfli.
- Quantization error is normalized RGBA RMSE after compositing each sheet over both the light maze
  background and a dark background. Lower values are better.
- Every Oxipng output was decoded and compared with its input. All variants had zero differing
  decoded pixels.

Benchmark tools and variants were kept under ignored `build/` directories and were not added as
project dependencies.

The reproducible project task uses the npm-lockfile versions pngquant 3.0.3 and Oxipng 8.0.0.
Oxipng 10.2.0 was retained as a benchmark rather than added as an untracked downloaded binary. The
current checked-in five-sheet output from `./gradlew processSpriteSheets` is 236,011 bytes. Only the
three sheets used by released gameplay are packaged, totaling 87,393 bytes.

## 128-Pixel Results

| Pipeline | Total bytes | Reduction from RGBA | Processing time |
|---|---:|---:|---:|
| Existing RGBA | 810,863 | - | - |
| ImageMagick 256-color | 246,141 | 69.6% | 0.397 s |
| ImageMagick + Oxipng level 4 | 214,618 | 73.5% | 0.961 s total |
| ImageMagick + Oxipng maximum | 212,616 | 73.8% | 2.848 s total |
| pngquant 256-color | 250,883 | 69.1% | 0.806 s |
| pngquant + Oxipng level 4 | 205,438 | 74.7% | 1.358 s total |
| pngquant + Oxipng maximum | 205,404 | 74.7% | 3.310 s total |
| pngquant + Oxipng maximum, then Zopfli | 205,041 | 74.7% | 5.883 s total |

Zopfli saved only 363 bytes (0.177%) after the maximum Oxipng pass. It is not worth adding to the
normal or release pipeline for these sheets.

pngquant produced lower measured palette error than ImageMagick (`0.0066351` versus `0.0075791`) and
the smaller final output after Oxipng. Native, 64-pixel, and 32-pixel comparisons showed no
noticeable loss from the selected 256-color/no-dither palette.

## 96-Pixel Results

The 96-pixel variant uses 84 pixels of content and a 6-pixel safe inset, preserving the 7:8 content
ratio of the 128/112 production layout.

| Pipeline | Total bytes | Reduction from matching RGBA |
|---|---:|---:|
| 96-pixel RGBA | 494,783 | - |
| ImageMagick 256-color | 153,003 | 69.1% |
| ImageMagick + Oxipng maximum | 137,544 | 72.2% |
| pngquant 256-color | 154,543 | 68.8% |
| pngquant + Oxipng maximum | 133,112 | 73.1% |
| pngquant + Oxipng maximum, then Zopfli | 132,933 | 73.1% |

The final 96-pixel set is 35.2% smaller than the final 128-pixel set and reduces decoded texture
pixels from 933,888 to 525,312 (43.8%). It remains visually comparable at 32, 64, and approximately
96 display pixels. It becomes visibly coarser when enlarged to 128 pixels.

The responsive layout does not cap desktop cell size. At a 1920x1080 viewport a 5x5 level can use
approximately 133-pixel cells, while a 7x7 level uses approximately 95-pixel cells. Higher-resolution
desktop viewports can exceed both values. A universal switch to 96-pixel assets would therefore
reduce quality on larger desktop layouts unless sprite rendering is capped or resolution-specific
assets are selected.

## Tight Atlas Result

A conservative atlas trial trimmed every frame and retained two transparent pixels of padding. It
reduced decoded texture area by 40.4%, from 933,888 to 556,511 pixels. The final indexed PNG set grew
slightly from 205,404 to 205,666 bytes because the existing repeated transparent cell structure
compresses efficiently.

Tight packing is useful only as a future GPU-memory optimization. It does not improve the current
download budget and would require atlas metadata plus runtime region/offset handling.

## Recommendation

1. Retain 128-pixel cells for the universal runtime derivatives.
2. Use pngquant at 256 colors with dithering disabled and its highest-quality palette search.
3. Follow quantization with Oxipng maximum optimization and safe metadata stripping.
4. Skip Zopfli; its sub-kilobyte gain does not justify another release step.
5. Keep 96-pixel generation available for a future bounded small-screen asset tier, but do not use it
   universally until rendering or asset selection guarantees that sprites will not be enlarged past
   their native resolution.
6. Treat tight atlas packing as a memory optimization only.
7. Implement optional-asset packaging and lazy browser loading through
   [`ASSET-01`](../roadmap/asset-01-lazy-delivery.md) before runtime integration of unused character
   or cosmetic sheets.

## Why These Files Are Larger Than Traditional Pixel Art

The source images use a pixel-art visual treatment, but they were authored as high-resolution
generated illustrations rather than as native low-resolution pixel art. Those are different asset
structures even when they look related on screen.

- The five atlases contain 57 frames. The checked-in output is 236,011 bytes total, averaging about
  4,141 bytes per frame. The Oxipng 10.2.0 benchmark reached 205,404 bytes, or about 3,604 bytes per
  frame. Both are already small for shaded 128-pixel artwork.
- Each character frame still uses many colors after per-sheet 256-color quantization. Advanced
  characters average about 192 colors per frame, advanced mice 210, basic characters 156, and the
  classic mouse 247. Traditional hand-authored pixel sprites often share a deliberately restricted
  palette measured in tens of colors.
- The source uses soft value changes, lighting gradients, textured clothing/fur, subpixel-looking
  edge colors, and small isolated detail pixels. Quantization maps those variations into the selected
  palette but does not make neighboring pixels repeat like broad flat-color pixel-art clusters.
- Frames are 128 by 128 cells. A native 32 by 32 sprite has one sixteenth as many pixels; a 64 by 64
  sprite has one quarter as many. Large transparent regions compress well, but the visible character
  pixels still dominate entropy.
- The frames include four independently rendered directions. Colors and shapes are similar but not
  exact transformations of one canonical view, limiting repetition between frames.
- PNG compression benefits from repeated byte patterns, not from the aesthetic label “pixel art.”
  Nearest-neighbor filtering preserves hard edges at runtime but does not make shaded source pixels
  cheaper to encode.

Achieving the much smaller sizes associated with classic sprite sheets would require changing more
than the container: redraw or carefully reduce the art at a lower native resolution, enforce one
small shared palette, replace gradients and generated texture with intentional flat clusters, and
reuse/mirror frames where the design permits. Those changes can preserve the identity of the art but
would create a visibly more traditional pixel-art style.

Useful upstream references:

- [pngquant](https://pngquant.org/) documents its perceptual palette generation, quality control, and
  no-dither operation.
- [Oxipng](https://github.com/oxipng/oxipng) documents its lossless optimization levels, palette
  sorting, metadata handling, and Zopfli mode.
- [libGDX TexturePacker](https://libgdx.com/wiki/tools/texture-packer) documents whitespace stripping
  and the atlas offsets required to reconstruct trimmed sprites.
