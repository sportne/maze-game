package io.github.sportne.mazegame.browser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

/** Captures comparable browser and artifact measurements during the release smoke flow. */
final class BrowserMetrics {
  private static final int FRAME_SAMPLE_COUNT = 120;

  private BrowserMetrics() {}

  static void capture(
      Page page,
      Path artifactDirectory,
      Path reportDirectory,
      String target,
      String browserEngine,
      String browserVersion,
      List<Long> firstFrameMillis,
      List<Double> responseEndMillis,
      Object usedHeap)
      throws IOException {
    ArtifactSize artifactSize = measureArtifact(artifactDirectory);
    List<Double> frameIntervals = measureFrameIntervals(page);
    List<String> report = new ArrayList<>();
    report.add("target=" + target);
    report.add("browser.engine=" + browserEngine);
    report.add("browser.version=" + browserVersion);
    report.add("touch.input=" + Boolean.getBoolean("mazeGame.touchInput"));
    report.add("artifact.bytes=" + artifactSize.bytes());
    report.add("artifact.gzip.bytes=" + artifactSize.gzipBytes());
    firstFrameMillis.sort(Comparator.naturalOrder());
    responseEndMillis.sort(Comparator.naturalOrder());
    report.add("startup.samples=" + firstFrameMillis.size());
    report.add(
        "navigation.responseEnd.median.millis=" + decimal(percentile(responseEndMillis, 0.50)));
    report.add("navigation.responseEnd.p95.millis=" + decimal(percentile(responseEndMillis, 0.95)));
    report.add("first.frame.median.millis=" + percentile(firstFrameMillis, 0.50));
    report.add("first.frame.p95.millis=" + percentile(firstFrameMillis, 0.95));
    report.add("frame.samples=" + frameIntervals.size());
    report.add("frame.interval.p50.millis=" + decimal(percentile(frameIntervals, 0.50)));
    report.add("frame.interval.p95.millis=" + decimal(percentile(frameIntervals, 0.95)));
    report.add("frame.interval.max.millis=" + decimal(frameIntervals.getLast()));
    report.add(
        "memory.usedJsHeap.bytes="
            + (usedHeap instanceof Number number ? number.longValue() : "unavailable"));
    Files.createDirectories(reportDirectory);
    Files.write(reportDirectory.resolve("metrics.properties"), report, StandardCharsets.UTF_8);
  }

  private static List<Double> measureFrameIntervals(Page page) {
    Object result =
        page.evaluate(
            "count => new Promise(resolve => {"
                + "const intervals = []; let previous;"
                + "function frame(timestamp) {"
                + "if (previous !== undefined) intervals.push(timestamp - previous);"
                + "previous = timestamp;"
                + "if (intervals.length === count) resolve(intervals);"
                + "else requestAnimationFrame(frame);"
                + "} requestAnimationFrame(frame); })",
            FRAME_SAMPLE_COUNT);
    assertTrue(result instanceof List<?>);
    List<Double> intervals = new ArrayList<>();
    for (Object value : (List<?>) result) {
      assertTrue(value instanceof Number);
      intervals.add(((Number) value).doubleValue());
    }
    assertFalse(intervals.isEmpty());
    intervals.sort(Comparator.naturalOrder());
    return intervals;
  }

  private static <T> T percentile(List<T> sortedValues, double percentile) {
    int index = Math.max(0, (int) Math.ceil(percentile * sortedValues.size()) - 1);
    return sortedValues.get(index);
  }

  private static ArtifactSize measureArtifact(Path artifactDirectory) throws IOException {
    long bytes = 0L;
    long gzipBytes = 0L;
    try (var paths = Files.walk(artifactDirectory)) {
      for (Path path : paths.filter(Files::isRegularFile).toList()) {
        bytes += Files.size(path);
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
          Files.copy(path, gzip);
        }
        gzipBytes += compressed.size();
      }
    }
    return new ArtifactSize(bytes, gzipBytes);
  }

  private static String decimal(double value) {
    return String.format(Locale.ROOT, "%.2f", value);
  }

  private record ArtifactSize(long bytes, long gzipBytes) {}
}
