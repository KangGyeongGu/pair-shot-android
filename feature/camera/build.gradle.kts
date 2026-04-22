plugins {
    id("pairshot.android.feature")
}

android {
    namespace = "com.pairshot.feature.camera"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:rendering"))
    implementation(project(":core:infra"))

    implementation(libs.camerax.compose)
    implementation(libs.camerax.view)
    implementation(libs.exifinterface)
}
