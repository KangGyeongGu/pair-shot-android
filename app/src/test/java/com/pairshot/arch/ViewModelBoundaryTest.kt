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
class ViewModelBoundaryTest {
    @ArchTest
    val `V-01 ViewModel should not use Context`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("android.content.Context")
            .because("ViewModel must not own Context — delegate to UseCase or Data layer")

    @ArchTest
    val `V-02 ViewModel should not use CameraX`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("androidx.camera..")
            .because("ViewModel must not own CameraX objects — delegate to UI Coordinator")

    @ArchTest
    val `V-03 ViewModel should not use hardware sensors`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("android.hardware..")
            .because("ViewModel must not own sensor managers — delegate to UI Coordinator")

    @ArchTest
    val `V-04 ViewModel should not use ExifInterface`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("androidx.exifinterface..")
            .because("ViewModel must not perform EXIF correction — delegate to Data/Core layer")

    @ArchTest
    val `V-05 ViewModel should not use MediaStore`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("android.provider.MediaStore")
            .because("ViewModel must not access MediaStore directly — delegate to Data layer")

    @ArchTest
    val `V-06 ViewModel should not use ContentResolver`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("android.content.ContentResolver")
            .because("ViewModel must not use ContentResolver — delegate to Data layer")

    @ArchTest
    val `V-07 ViewModel should not use java io File`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("java.io.File")
            .because("ViewModel must not manipulate files — delegate to Data layer")

    @ArchTest
    val `V-08 ViewModel should not use ZipOutputStream`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("java.util.zip..")
            .because("ViewModel must not create ZIP — delegate to Data layer")

    @ArchTest
    val `V-09 ViewModel should not use ActivityResultLauncher`: ArchRule =
        noClasses()
            .that()
            .areAssignableTo(androidx.lifecycle.ViewModel::class.java)
            .should()
            .dependOnClassesThat()
            .haveFullyQualifiedName("androidx.activity.result.ActivityResultLauncher")
            .because("ViewModel must not own ActivityResultLauncher — belongs to UI layer")
}
