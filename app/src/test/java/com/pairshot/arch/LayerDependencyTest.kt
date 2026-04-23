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
    val `L-01 Feature should not directly access Data layer Impl`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("com.pairshot.feature..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.pairshot.core.data.repository..")
            .because("Feature must access Data only through Domain layer interfaces")

    @ArchTest
    val `L-02 Data should not access Feature`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("com.pairshot.core.data..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.pairshot.feature..")
            .because("Data layer must not depend on Feature layer")

    @ArchTest
    val `L-03 Domain should not access Data`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("com.pairshot.core.domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.pairshot.core.data..")
            .because("Domain layer must not depend on Data layer")

    @ArchTest
    val `L-04 layered architecture`: ArchRule =
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Feature")
            .definedBy("com.pairshot.feature..")
            .layer("Data")
            .definedBy("com.pairshot.core.data..")
            .layer("Domain")
            .definedBy("com.pairshot.core.domain..")
            .layer("Infra")
            .definedBy("com.pairshot.core.infra..")
            .layer("AppShell")
            .definedBy("com.pairshot.app..", "com.pairshot.di..")
            .whereLayer("Feature")
            .mayOnlyBeAccessedByLayers("AppShell")
            .whereLayer("Data")
            .mayOnlyBeAccessedByLayers("AppShell")
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Feature", "Data", "Infra", "AppShell")
            .whereLayer("Infra")
            .mayOnlyBeAccessedByLayers("Feature", "Data", "AppShell")
}
