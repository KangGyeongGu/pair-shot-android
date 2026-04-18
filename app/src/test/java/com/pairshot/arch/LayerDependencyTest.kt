package com.pairshot.arch

import com.pairshot.arch.config.DoNotIncludeAndroidGenerated
import com.pairshot.arch.config.DoNotIncludeKotlinWhenMappings
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture

@AnalyzeClasses(
    packages = ["com.pairshot"],
    importOptions = [
        ImportOption.DoNotIncludeTests::class,
        DoNotIncludeAndroidGenerated::class,
        DoNotIncludeKotlinWhenMappings::class,
    ],
)
class LayerDependencyTest {
    @ArchTest
    val `L-01 UI should not directly access Data`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..ui..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.pairshot.data..")
            .because("UI layer must access Data only through Domain layer")

    @ArchTest
    val `L-02 Data should not access UI`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..data..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..ui..")
            .because("Data layer must not depend on UI layer")

    @ArchTest
    val `L-03 Domain should not access Data`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.pairshot.data..")
            .because("Domain layer must not depend on Data layer")

    @ArchTest
    val `L-04 layered architecture`: ArchRule =
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Feature-UI")
            .definedBy("com.pairshot.feature..ui..")
            .layer("Data")
            .definedBy("com.pairshot.data..")
            .layer("DI")
            .definedBy("com.pairshot.di..")
            .layer("Core")
            .definedBy("com.pairshot.core..")
            .layer("AppShell")
            .definedBy("com.pairshot.app..")
            .whereLayer("Feature-UI")
            .mayOnlyBeAccessedByLayers("AppShell")
            .whereLayer("Data")
            .mayOnlyBeAccessedByLayers("DI", "Core")
            .whereLayer("Core")
            .mayOnlyBeAccessedByLayers(
                "Feature-UI",
                "Data",
                "DI",
                "AppShell",
            )
}
