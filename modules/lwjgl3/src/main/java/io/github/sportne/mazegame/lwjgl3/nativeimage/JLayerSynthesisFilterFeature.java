package io.github.sportne.mazegame.lwjgl3.nativeimage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

/**
 * Prepares JLayer's MP3 decoder table for GraalVM native images.
 *
 * <p>JLayer lazily reads serialized synthesis filter data at runtime. Native images need that data
 * available without normal classpath resource loading, so this feature reads {@code /sfd.ser}
 * during image analysis and installs the decoded arrays into JLayer's static fields.
 */
public final class JLayerSynthesisFilterFeature implements Feature {
  /** Number of float values in each row of JLayer's split synthesis filter table. */
  private static final int SYNTHESIS_FILTER_STRIDE = 16;

  /**
   * Initializes JLayer's synthesis filter data during native-image analysis.
   *
   * @param access GraalVM analysis API for looking up application classes
   */
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

  /**
   * Reads the serialized JLayer synthesis filter table bundled as a resource.
   *
   * @return decoded synthesis filter values
   */
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

  /**
   * Splits JLayer's flat synthesis filter table into fixed-size rows.
   *
   * @param values flat synthesis filter values
   * @return two-dimensional table expected by JLayer
   */
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

  /**
   * Writes a static field through reflection while building the native image.
   *
   * @param declaringClass class that owns the static field
   * @param fieldName field to update
   * @param value value to store in the field
   */
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
