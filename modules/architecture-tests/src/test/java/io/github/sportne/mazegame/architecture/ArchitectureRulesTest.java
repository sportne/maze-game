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
  static final ArchRule coreCodeDoesNotDependOnConcreteBackends =
      noClasses()
          .that()
          .resideOutsideOfPackages("..lwjgl3..", "..teavm..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.badlogic.gdx.backends.lwjgl3..",
              "com.github.xpenatan.gdx.teavm.backends..",
              "org.lwjgl..");

  @ArchTest
  static final ArchRule browserReachableCodeDoesNotUseJvmFilesystem =
      noClasses()
          .that()
          .resideOutsideOfPackage("..lwjgl3..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("java.nio.file..");

  @ArchTest
  static final ArchRule browserReachableCodeDoesNotUseLegacyJvmFiles =
      noClasses()
          .that()
          .resideOutsideOfPackage("..lwjgl3..")
          .should()
          .dependOnClassesThat()
          .haveFullyQualifiedName("java.io.File");

  @ArchTest
  static final ArchRule browserReachableCodeDoesNotReadEnvironmentVariableMap =
      noClasses()
          .that()
          .resideOutsideOfPackage("..lwjgl3..")
          .should()
          .callMethod(System.class, "getenv");

  @ArchTest
  static final ArchRule browserReachableCodeDoesNotReadEnvironmentVariablesByName =
      noClasses()
          .that()
          .resideOutsideOfPackage("..lwjgl3..")
          .should()
          .callMethod(System.class, "getenv", String.class);

  @ArchTest
  static final ArchRule browserReachableCodeDoesNotReadSystemProperties =
      noClasses()
          .that()
          .resideOutsideOfPackage("..lwjgl3..")
          .should()
          .callMethod(System.class, "getProperty", String.class);

  @ArchTest
  static final ArchRule browserReachableCodeDoesNotReadSystemPropertiesWithDefaults =
      noClasses()
          .that()
          .resideOutsideOfPackage("..lwjgl3..")
          .should()
          .callMethod(System.class, "getProperty", String.class, String.class);

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
          .resideInAnyPackage("..model.level..", "..model.maze..", "..model.solver..");

  @ArchTest
  static final ArchRule levelDefinitionsDoNotDependOnRuntimeDomainPackages =
      noClasses()
          .that()
          .resideInAPackage("..model.level..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..model.maze..", "..model.solver..");

  @ArchTest
  static final ArchRule mazeStateDoesNotDependOnSolverSimulation =
      noClasses()
          .that()
          .resideInAPackage("..model.maze..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..model.solver..");

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
