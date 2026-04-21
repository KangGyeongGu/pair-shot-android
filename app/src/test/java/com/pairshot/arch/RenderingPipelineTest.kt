package com.pairshot.arch

import com.pairshot.arch.config.DoNotIncludeAndroidGenerated
import com.pairshot.arch.config.DoNotIncludeKotlinWhenMappings
import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

@AnalyzeClasses(
    packages = ["com.pairshot"],
    importOptions = [
        ImportOption.DoNotIncludeTests::class,
        DoNotIncludeAndroidGenerated::class,
        DoNotIncludeKotlinWhenMappings::class,
    ],
)
class RenderingPipelineTest {
    @ArchTest
    val `R-01 Canvas should only be used in core util or data`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("com.pairshot.feature..")
            .should()
            .dependOnClassesThat(
                object : DescribedPredicate<JavaClass>("are android.graphics.Canvas") {
                    override fun test(clazz: JavaClass): Boolean = clazz.fullName == "android.graphics.Canvas"
                },
            ).because("Bitmap Canvas rendering must go through central pipeline in core/util or data layer")

    @ArchTest
    val `R-02 BitmapFactory should only be used in core util or data`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("com.pairshot.feature..")
            .should()
            .dependOnClassesThat(
                object : DescribedPredicate<JavaClass>("are android.graphics.BitmapFactory") {
                    override fun test(clazz: JavaClass): Boolean = clazz.fullName == "android.graphics.BitmapFactory"
                },
            ).because("Bitmap decoding must go through central loader in core/util — not in feature code")
}
