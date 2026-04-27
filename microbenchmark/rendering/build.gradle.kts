plugins {
    id("pairshot.android.library")
}

android {
    namespace = "com.pairshot.microbenchmark.rendering"
    defaultConfig {
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }
    testBuildType = "release"
    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core:rendering"))
    implementation(project(":core:model"))

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.benchmark.junit4)
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
