package io.github.sportne.mazegame.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "io.github.sportne.mazegame",
    importOptions = ImportOption.DoNotIncludeTests.class)
final class ArchitectureRulesTest {
  @ArchTest
  static final ArchRule coreCodeDoesNotDependOnTheLwjglBackend =
      noClasses()
          .that()
          .resideOutsideOfPackage("..lwjgl3..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("com.badlogic.gdx.backends.lwjgl3..", "org.lwjgl..");

  @ArchTest
  static final ArchRule oldGeneratedSamplePackagesAreNotUsed =
      noClasses().should().resideInAnyPackage("org.example..");

  @ArchTest
  static final ArchRule domainModelDoesNotDependOnAdaptersOrSessionState =
      noClasses()
          .that()
          .resideInAPackage("..model..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.badlogic.gdx..",
              "..assets..",
              "..debug..",
              "..input..",
              "..layout..",
              "..lwjgl3..",
              "..render..",
              "..state..");

  @ArchTest
  static final ArchRule gridPrimitivesDoNotDependOnHigherLevelDomainPackages =
      noClasses()
          .that()
          .resideInAPackage("..model.grid..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..model.level..", "..model.maze..", "..model.mouse..");

  @ArchTest
  static final ArchRule levelDefinitionsDoNotDependOnRuntimeDomainPackages =
      noClasses()
          .that()
          .resideInAPackage("..model.level..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..model.maze..", "..model.mouse..");

  @ArchTest
  static final ArchRule mazeStateDoesNotDependOnMouseSimulation =
      noClasses()
          .that()
          .resideInAPackage("..model.maze..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..model.mouse..");

  @ArchTest
  static final ArchRule sessionStateDoesNotDependOnAdapters =
      noClasses()
          .that()
          .resideInAPackage("..state..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.badlogic.gdx..",
              "..assets..",
              "..debug..",
              "..input..",
              "..layout..",
              "..lwjgl3..",
              "..render..");

  @ArchTest
  static final ArchRule publicStaticFieldsAreConstants =
      fields().that().arePublic().and().areStatic().should().beFinal().allowEmptyShould(true);

  private ArchitectureRulesTest() {}
}
