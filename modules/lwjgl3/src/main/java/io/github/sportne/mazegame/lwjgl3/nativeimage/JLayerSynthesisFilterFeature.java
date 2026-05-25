package io.github.sportne.mazegame.lwjgl3.nativeimage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

/** Prepares JLayer's MP3 decoder table for GraalVM native images. */
public final class JLayerSynthesisFilterFeature implements Feature {
  private static final int SYNTHESIS_FILTER_STRIDE = 16;

  @Override
  public void beforeAnalysis(BeforeAnalysisAccess access) {
    Class<?> synthesisFilter = access.findClassByName("javazoom.jl.decoder.SynthesisFilter");
    if (synthesisFilter == null) {
      return;
    }
    RuntimeClassInitialization.initializeAtBuildTime(synthesisFilter);
    float[] synthesisFilterTable = readSynthesisFilterTable();
    setStaticField(synthesisFilter, "d", synthesisFilterTable);
    setStaticField(synthesisFilter, "d16", splitArray(synthesisFilterTable));
  }

  private static float[] readSynthesisFilterTable() {
    try (InputStream input = JLayerSynthesisFilterFeature.class.getResourceAsStream("/sfd.ser")) {
      if (input == null) {
        throw new IllegalStateException("JLayer synthesis filter table resource is missing.");
      }
      try (ObjectInputStream objectInput = new ObjectInputStream(input)) {
        return (float[]) objectInput.readObject();
      }
    } catch (IOException | ClassNotFoundException exception) {
      throw new IllegalStateException("Unable to read JLayer synthesis filter table.", exception);
    }
  }

  private static float[][] splitArray(float[] values) {
    float[][] chunks = new float[values.length / SYNTHESIS_FILTER_STRIDE][SYNTHESIS_FILTER_STRIDE];
    for (int chunkIndex = 0; chunkIndex < chunks.length; chunkIndex++) {
      System.arraycopy(
          values,
          chunkIndex * SYNTHESIS_FILTER_STRIDE,
          chunks[chunkIndex],
          0,
          SYNTHESIS_FILTER_STRIDE);
    }
    return chunks;
  }

  private static void setStaticField(Class<?> declaringClass, String fieldName, Object value) {
    try {
      Field field = declaringClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(null, value);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Unable to prepare JLayer synthesis filter.", exception);
    }
  }
}
