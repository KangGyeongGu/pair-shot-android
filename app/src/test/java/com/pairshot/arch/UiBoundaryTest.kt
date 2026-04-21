package com.pairshot.arch

import com.pairshot.arch.config.DoNotIncludeAndroidGenerated
import com.pairshot.arch.config.DoNotIncludeKotlinWhenMappings
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
class UiBoundaryTest {
    @ArchTest
    val `U-01 UI should not use ExifInterface`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..ui..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("androidx.exifinterface..")
            .because("UI must not parse EXIF directly — use shared loader in core/util")

    @ArchTest
    val `U-02 UI should not use BitmapFactory`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..ui..")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("android.graphics.BitmapFactory")
            .because("UI must not decode bitmaps directly — use shared loader in core/util")

    @ArchTest
    val `U-03 UI should not use MediaStore`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..ui..")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("android.provider.MediaStore")
            .because("UI must not access MediaStore — delegate to Data layer")

    @ArchTest
    val `U-04 UI should not use ZipOutputStream`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..ui..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("java.util.zip..")
            .because("UI must not create ZIP — delegate to Data layer")

    @ArchTest
    val `U-05 UI should not use ContentResolver`: ArchRule =
        noClasses()
            .that()
            .resideInAPackage("..ui..")
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("android.content.ContentResolver")
            .because("UI must not use ContentResolver — delegate to Data layer")
}
