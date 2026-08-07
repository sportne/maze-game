package io.github.sportne.mazegame.model.level;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable ordered catalog of authored levels keyed by stable level id. */
public final class LevelCatalog {
  /** Levels in display order. */
  private final List<LevelDefinition> levels;

  /** Levels indexed by stable id. */
  private final Map<String, LevelDefinition> levelsById;

  /**
   * Creates a non-empty catalog with unique stable ids.
   *
   * @param levels authored levels in display order
   */
  public LevelCatalog(List<LevelDefinition> levels) {
    Objects.requireNonNull(levels, "levels");
    if (levels.isEmpty()) {
      throw new IllegalArgumentException("level catalog must not be empty");
    }
    this.levels = List.copyOf(levels);
    Map<String, LevelDefinition> indexedLevels = new LinkedHashMap<>();
    for (LevelDefinition level : this.levels) {
      Objects.requireNonNull(level, "level");
      if (indexedLevels.putIfAbsent(level.id(), level) != null) {
        throw new IllegalArgumentException("duplicate level id: ".concat(level.id()));
      }
    }
    levelsById = Map.copyOf(indexedLevels);
  }

  /**
   * Returns authored levels in stable display order.
   *
   * @return immutable ordered levels
   */
  public List<LevelDefinition> levels() {
    return levels;
  }

  /**
   * Finds an authored level by stable id.
   *
   * @param levelId stable level id
   * @return matching definition, or empty
   */
  public Optional<LevelDefinition> findById(String levelId) {
    Objects.requireNonNull(levelId, "levelId");
    return Optional.ofNullable(levelsById.get(levelId));
  }
}
