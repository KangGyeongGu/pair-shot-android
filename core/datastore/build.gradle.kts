plugins {
    id("pairshot.android.library")
}

android {
    namespace = "com.pairshot.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.datastore.preferences)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
