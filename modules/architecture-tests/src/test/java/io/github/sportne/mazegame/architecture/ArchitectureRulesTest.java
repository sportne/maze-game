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
  static final ArchRule publicStaticFieldsAreConstants =
      fields().that().arePublic().and().areStatic().should().beFinal().allowEmptyShould(true);

  private ArchitectureRulesTest() {}
}
