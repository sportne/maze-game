package io.github.sportne.mazegame.model.cell;

import java.util.OptionalInt;

/** Validated finite or infinite supply for one placeable cell type. */
public final class CellSupply {
  private static final CellSupply INFINITE = new CellSupply(true, 0);

  private final boolean infinite;
  private final int count;

  private CellSupply(boolean infinite, int count) {
    if (count < 0) {
      throw new IllegalArgumentException("finite supply must not be negative");
    }
    this.infinite = infinite;
    this.count = count;
  }

  /** Creates a finite supply, including an exhausted zero supply. */
  public static CellSupply finite(int count) {
    return new CellSupply(false, count);
  }

  /** Returns the shared infinite-supply value. */
  public static CellSupply infinite() {
    return INFINITE;
  }

  /** Returns whether this supply has no finite limit. */
  public boolean isInfinite() {
    return infinite;
  }

  /** Returns the finite count, or an empty value for infinite supply. */
  public OptionalInt finiteCount() {
    return infinite ? OptionalInt.empty() : OptionalInt.of(count);
  }

  /** Returns whether at least one item can be placed. */
  public boolean available() {
    return infinite || count > 0;
  }

  /** Returns supply after consuming one item, or throws when finite supply is exhausted. */
  public CellSupply consume() {
    if (!available()) {
      throw new IllegalStateException("supply is exhausted");
    }
    return infinite ? this : finite(count - 1);
  }

  @Override
  public boolean equals(Object other) {
    return this == other
        || (other instanceof CellSupply supply
            && infinite == supply.infinite
            && count == supply.count);
  }

  @Override
  public int hashCode() {
    return 31 * Boolean.hashCode(infinite) + count;
  }

  @Override
  public String toString() {
    return infinite ? "CellSupply[infinite]" : "CellSupply[finite=" + count + "]";
  }
}
