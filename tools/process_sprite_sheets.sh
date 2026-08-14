#!/usr/bin/env bash

set -euo pipefail

readonly CELL_SIZE="${SPRITE_CELL_SIZE:-128}"
readonly CONTENT_SIZE="${SPRITE_CONTENT_SIZE:-$((CELL_SIZE * 7 / 8))}"
readonly BACKGROUND_THRESHOLD='4%'

if ((CELL_SIZE <= 0 || CONTENT_SIZE <= 0 || CONTENT_SIZE > CELL_SIZE)); then
  printf 'Sprite cell/content sizes must be positive and content must fit within the cell.\n' >&2
  exit 1
fi

readonly PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly SOURCE_DIRECTORY="${PROJECT_ROOT}/assets"
readonly OUTPUT_DIRECTORY="${1:-${SOURCE_DIRECTORY}/processed}"

if command -v magick >/dev/null 2>&1; then
  image_convert() {
    magick -limit thread 1 "$@"
  }

  image_identify() {
    magick identify "$@"
  }
elif command -v convert >/dev/null 2>&1 && command -v identify >/dev/null 2>&1; then
  image_convert() {
    convert -limit thread 1 "$@"
  }

  image_identify() {
    identify "$@"
  }
else
  printf 'ImageMagick is required (magick, or convert and identify).\n' >&2
  exit 1
fi

resolve_tool() {
  local override="$1"
  local command_name="$2"

  if [[ -n "${override}" ]]; then
    printf '%s\n' "${override}"
    return
  fi
  if command -v "${command_name}" >/dev/null 2>&1; then
    command -v "${command_name}"
    return
  fi
  printf 'Required sprite-processing tool is unavailable: %s.\n' "${command_name}" >&2
  exit 1
}

readonly PNGQUANT_COMMAND="$(resolve_tool "${PNGQUANT:-}" pngquant)"
readonly OXIPNG_COMMAND="$(resolve_tool "${OXIPNG:-}" oxipng)"

temporary_directory="$(mktemp -d "${TMPDIR:-/tmp}/maze-game-sprites.XXXXXX")"
trap 'rm -rf "${temporary_directory}"' EXIT

mkdir -p "${OUTPUT_DIRECTORY}"

extract_cutout() {
  local source_file="$1"
  local left="$2"
  local right="$3"
  local top="$4"
  local bottom="$5"
  local column="$6"
  local row="$7"
  local destination="$8"
  local crop_width crop_height
  local crop_file mask_seed_file mask_file channels

  crop_width=$((right - left))
  crop_height=$((bottom - top))

  crop_file="${temporary_directory}/crop-${row}-${column}.png"
  mask_seed_file="${temporary_directory}/mask-seed-${row}-${column}.png"
  mask_file="${temporary_directory}/mask-${row}-${column}.png"

  image_convert "${source_file}" \
    -crop "${crop_width}x${crop_height}+${left}+${top}" \
    +repage \
    "${crop_file}"

  channels="$(image_identify -format '%[channels]' "${source_file}")"
  if [[ "${channels}" == *a* ]]; then
    image_convert "${crop_file}" -trim +repage "${destination}"
    return
  fi

  # Start the matte with pixels distinguishable from the near-black source background.
  # Closing reconnects isolated pixel-art details without erasing dark interior features.
  image_convert "${crop_file}" \
    -colorspace gray \
    -threshold "${BACKGROUND_THRESHOLD}" \
    -morphology Close Diamond:1 \
    "${mask_seed_file}"

  # Component zero is the border-connected black background. Thresholding the component labels
  # keeps every enclosed component opaque, including black eyes and outlines, without a recursive
  # flood fill that can be unreliable under constrained ImageMagick resource policies.
  image_convert "${mask_seed_file}" \
    -connected-components 4 \
    -threshold 0 \
    "${mask_file}"

  image_convert "${crop_file}" "${mask_file}" \
    -alpha off \
    -compose CopyOpacity \
    -composite \
    -trim \
    +repage \
    "${destination}"
}

find_inner_gutters() {
  local source_file="$1"
  local axis="$2"
  local expected_count="$3"
  local source_width source_height activity_geometry coordinate_field axis_length activity_file channels

  read -r source_width source_height < <(image_identify -format '%w %h\n' "${source_file}")
  if [[ "${axis}" == 'x' ]]; then
    activity_geometry="${source_width}x1!"
    coordinate_field=1
    axis_length="${source_width}"
  else
    activity_geometry="1x${source_height}!"
    coordinate_field=2
    axis_length="${source_height}"
  fi

  activity_file="${temporary_directory}/activity-$(basename "${source_file}")-${axis}.txt"
  channels="$(image_identify -format '%[channels]' "${source_file}")"
  if [[ "${channels}" == *a* ]]; then
    image_convert "${source_file}" \
      -alpha extract \
      -threshold 0 \
      -filter box \
      -resize "${activity_geometry}" \
      "txt:${activity_file}"
  else
    image_convert "${source_file}" \
      -colorspace gray \
      -threshold "${BACKGROUND_THRESHOLD}" \
      -filter box \
      -resize "${activity_geometry}" \
      "txt:${activity_file}"
  fi

  awk -F'[,()]' \
    -v coordinate_field="${coordinate_field}" \
    -v axis_length="${axis_length}" \
    -v expected_count="${expected_count}" '
      NR > 1 {
        coordinate = $coordinate_field
        sub(/:.*/, "", coordinate)
        value = $7 + 0
        if (value == 0 && start == "") {
          start = coordinate
        }
        if (value != 0 && start != "") {
          emit_gutter(start, coordinate - 1)
          start = ""
        }
      }
      END {
        if (start != "") {
          emit_gutter(start, axis_length - 1)
        }
        if (found != expected_count) {
          printf "Expected %d internal gutters but found %d.\n", expected_count, found > "/dev/stderr"
          exit 1
        }
      }
      function emit_gutter(first, last) {
        if (first > 0 && last < axis_length - 1 && last - first + 1 >= 8) {
          print int((first + last + 1) / 2)
          found += 1
        }
      }
    ' "${activity_file}"
}

normalize_frame() {
  local frame="$1"
  local max_width="$2"
  local max_height="$3"
  local destination="$4"

  image_convert "${frame}" \
    -background none \
    -gravity south \
    -extent "${max_width}x${max_height}" \
    -filter point \
    -resize "${CONTENT_SIZE}x${CONTENT_SIZE}" \
    -gravity center \
    -extent "${CELL_SIZE}x${CELL_SIZE}" \
    "${destination}"
}

process_sheet() {
  local source_name="$1"
  local output_name="$2"
  local columns="$3"
  local rows="$4"
  local source_file="${SOURCE_DIRECTORY}/${source_name}"
  local work_directory="${temporary_directory}/${output_name%.png}"
  local max_width=0 max_height=0 row column width height frame normalized row_file
  local source_width source_height left right top bottom
  local -a x_gutters y_gutters x_boundaries y_boundaries row_frames assembled_rows

  if [[ ! -f "${source_file}" ]]; then
    printf 'Missing source sprite sheet: %s\n' "${source_file}" >&2
    exit 1
  fi

  mkdir -p "${work_directory}"

  read -r source_width source_height < <(image_identify -format '%w %h\n' "${source_file}")
  mapfile -t x_gutters < <(find_inner_gutters "${source_file}" x "$((columns - 1))")
  mapfile -t y_gutters < <(find_inner_gutters "${source_file}" y "$((rows - 1))")
  x_boundaries=(0 "${x_gutters[@]}" "${source_width}")
  y_boundaries=(0 "${y_gutters[@]}" "${source_height}")

  for ((row = 0; row < rows; row += 1)); do
    for ((column = 0; column < columns; column += 1)); do
      frame="${work_directory}/cutout-${row}-${column}.png"
      left="${x_boundaries[column]}"
      right="${x_boundaries[column + 1]}"
      top="${y_boundaries[row]}"
      bottom="${y_boundaries[row + 1]}"
      extract_cutout \
        "${source_file}" \
        "${left}" \
        "${right}" \
        "${top}" \
        "${bottom}" \
        "${column}" \
        "${row}" \
        "${frame}"
      read -r width height < <(image_identify -format '%w %h\n' "${frame}")
      if ((width > max_width)); then
        max_width="${width}"
      fi
      if ((height > max_height)); then
        max_height="${height}"
      fi
    done
  done

  for ((row = 0; row < rows; row += 1)); do
    row_frames=()
    for ((column = 0; column < columns; column += 1)); do
      frame="${work_directory}/cutout-${row}-${column}.png"
      normalized="${work_directory}/normalized-${row}-${column}.png"
      normalize_frame "${frame}" "${max_width}" "${max_height}" "${normalized}"
      row_frames+=("${normalized}")
    done
    row_file="${work_directory}/row-${row}.png"
    image_convert "${row_frames[@]}" +append "${row_file}"
    assembled_rows+=("${row_file}")
  done

  image_convert "${assembled_rows[@]}" \
    -append \
    +repage \
    -strip \
    -define png:exclude-chunk=all \
    -define png:compression-level=9 \
    "${OUTPUT_DIRECTORY}/${output_name}"
}

process_classic_mouse_assets() {
  local source_file="${SOURCE_DIRECTORY}/mouse-sprites.png"
  local work_directory="${temporary_directory}/classic-mouse"
  local source_width source_height split_x goal_x split_y index width height max_width=0 max_height=0
  local frame normalized left right top bottom
  local -a regions frames normalized_frames

  if [[ ! -f "${source_file}" ]]; then
    printf 'Missing source sprite sheet: %s\n' "${source_file}" >&2
    exit 1
  fi

  mkdir -p "${work_directory}"
  read -r source_width source_height < <(image_identify -format '%w %h\n' "${source_file}")
  split_x=$((source_width * 5 / 12))
  goal_x=$((source_width * 3 / 4))
  split_y=$((source_height / 2))
  regions=(
    "0,${split_x},0,${split_y}"
    "${split_x},${goal_x},0,${split_y}"
    "0,${split_x},${split_y},${source_height}"
    "${split_x},${goal_x},${split_y},${source_height}"
  )

  for ((index = 0; index < 4; index += 1)); do
    IFS=',' read -r left right top bottom <<<"${regions[index]}"
    frame="${work_directory}/cutout-${index}.png"
    extract_cutout "${source_file}" "${left}" "${right}" "${top}" "${bottom}" "${index}" 0 "${frame}"
    frames+=("${frame}")
    read -r width height < <(image_identify -format '%w %h\n' "${frame}")
    if ((width > max_width)); then
      max_width="${width}"
    fi
    if ((height > max_height)); then
      max_height="${height}"
    fi
  done

  for ((index = 0; index < 4; index += 1)); do
    normalized="${work_directory}/normalized-${index}.png"
    normalize_frame "${frames[index]}" "${max_width}" "${max_height}" "${normalized}"
    normalized_frames+=("${normalized}")
  done
  image_convert "${normalized_frames[@]}" \
    +append \
    +repage \
    -strip \
    -define png:exclude-chunk=all \
    -define png:compression-level=9 \
    "${OUTPUT_DIRECTORY}/classic-mouse.png"

}

optimize_runtime_sheet() {
  local output_file="$1"
  local indexed_file="${temporary_directory}/indexed-$(basename "${output_file}")"

  "${PNGQUANT_COMMAND}" \
    --force \
    --nofs \
    --speed 1 \
    --quality 0-100 \
    --strip \
    --output "${indexed_file}" \
    256 \
    -- \
    "${output_file}"
  "${OXIPNG_COMMAND}" \
    --quiet \
    --threads 1 \
    --strip safe \
    -o max \
    "${indexed_file}"
  cp "${indexed_file}" "${output_file}"
}

validate_sheet() {
  local output_name="$1"
  local columns="$2"
  local rows="$3"
  local output_file="${OUTPUT_DIRECTORY}/${output_name}"
  local expected_width=$((columns * CELL_SIZE))
  local expected_height=$((rows * CELL_SIZE))
  local actual_width actual_height channels row column frame_width frame_height offset_x offset_y

  read -r actual_width actual_height channels < <(
    image_identify -format '%w %h %[channels]\n' "${output_file}"
  )
  if ((actual_width != expected_width || actual_height != expected_height)); then
    printf \
      'Unexpected dimensions for %s: expected %dx%d, found %dx%d.\n' \
      "${output_file}" \
      "${expected_width}" \
      "${expected_height}" \
      "${actual_width}" \
      "${actual_height}" >&2
    exit 1
  fi
  if [[ "${channels}" != *a* ]]; then
    printf 'Expected an alpha channel in %s, found %s.\n' "${output_file}" "${channels}" >&2
    exit 1
  fi

  for ((row = 0; row < rows; row += 1)); do
    for ((column = 0; column < columns; column += 1)); do
      read -r frame_width frame_height offset_x offset_y < <(
        image_convert "${output_file}" \
          -crop "${CELL_SIZE}x${CELL_SIZE}+$((column * CELL_SIZE))+$((row * CELL_SIZE))" \
          +repage \
          -alpha extract \
          -trim \
          -format '%w %h %X %Y\n' \
          info:
      )
      if ((
        frame_width > CONTENT_SIZE ||
        frame_height > CONTENT_SIZE ||
        offset_x < (CELL_SIZE - CONTENT_SIZE) / 2 ||
        offset_y < (CELL_SIZE - CONTENT_SIZE) / 2 ||
        offset_x + frame_width > CELL_SIZE - (CELL_SIZE - CONTENT_SIZE) / 2 ||
        offset_y + frame_height > CELL_SIZE - (CELL_SIZE - CONTENT_SIZE) / 2
      )); then
        printf \
          'Frame %d,%d in %s exceeds its %dx%d safe content area.\n' \
          "${column}" \
          "${row}" \
          "${output_file}" \
          "${CONTENT_SIZE}" \
          "${CONTENT_SIZE}" >&2
        exit 1
      fi
    done
  done
}

process_sheet 'basic-character-sprites.png' 'basic-characters.png' 4 4
process_sheet 'advanced-character-sprites.png' 'advanced-characters.png' 4 4
process_sheet 'advanced-mouse-sprites.png' 'advanced-mice.png' 4 4
process_sheet 'goal-sprites.png' 'goals.png' 5 1
process_classic_mouse_assets

for output_file in \
  "${OUTPUT_DIRECTORY}/basic-characters.png" \
  "${OUTPUT_DIRECTORY}/advanced-characters.png" \
  "${OUTPUT_DIRECTORY}/advanced-mice.png" \
  "${OUTPUT_DIRECTORY}/goals.png" \
  "${OUTPUT_DIRECTORY}/classic-mouse.png"; do
  optimize_runtime_sheet "${output_file}"
done

validate_sheet 'basic-characters.png' 4 4
validate_sheet 'advanced-characters.png' 4 4
validate_sheet 'advanced-mice.png' 4 4
validate_sheet 'goals.png' 5 1
validate_sheet 'classic-mouse.png' 4 1

for output_file in \
  "${OUTPUT_DIRECTORY}/basic-characters.png" \
  "${OUTPUT_DIRECTORY}/advanced-characters.png" \
  "${OUTPUT_DIRECTORY}/advanced-mice.png" \
  "${OUTPUT_DIRECTORY}/goals.png" \
  "${OUTPUT_DIRECTORY}/classic-mouse.png"; do
  image_identify -format '%f %wx%h %[channels] %b\n' "${output_file}"
done
