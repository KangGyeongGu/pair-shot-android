plugins {
    id("pairshot.android.library")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pairshot.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:storage"))
    implementation(project(":core:infra"))
    implementation(project(":core:rendering"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation(libs.timber)
}
