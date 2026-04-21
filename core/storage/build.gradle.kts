plugins {
    id("pairshot.android.library")
}

android {
    namespace = "com.pairshot.core.storage"
}

dependencies {
    implementation(project(":core:model"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation(libs.timber)
}
