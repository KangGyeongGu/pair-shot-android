import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) load(file.inputStream())
    }

android {
    namespace = "com.pairshot"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pairshot"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile =
                file(
                    System.getenv("KEYSTORE_PATH")
                        ?: localProperties["KEYSTORE_PATH"] as String,
                )
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: localProperties["KEYSTORE_PASSWORD"] as String
            keyAlias = System.getenv("KEY_ALIAS")
                ?: localProperties["KEY_ALIAS"] as String
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: localProperties["KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(project(":core:rendering"))
    implementation(project(":core:infra"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:storage"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))

    implementation(project(":feature:camera"))
    implementation(project(":feature:gallery"))
    implementation(project(":feature:compare"))
    implementation(project(":feature:export"))
    implementation(project(":feature:project"))
    implementation(project(":feature:settings"))

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.tooling.preview)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.compose)
    implementation(libs.camerax.extensions)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Location
    implementation(libs.play.services.location)

    // DataStore
    implementation(libs.datastore.preferences)

    // Activity Compose
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    // Coil
    implementation(libs.coil.compose)

    // ExifInterface
    implementation(libs.exifinterface)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Concurrent Futures (ListenableFuture.await())
    implementation(libs.concurrent.futures.ktx)

    // ProfileInstaller
    implementation(libs.profileinstaller)

    // ColorPicker
    implementation(libs.colorpicker.compose)

    // Timber
    implementation(libs.timber)

    // JankStats
    implementation(libs.jankstats)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.archunit.junit5)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.compose.ui.test)
}
