plugins {
    id("pairshot.android.library")
}

android {
    namespace = "com.pairshot.core.rendering"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.exifinterface)
}
