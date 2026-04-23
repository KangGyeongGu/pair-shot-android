plugins {
    id("pairshot.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pairshot.core.storage"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation(libs.timber)
}
