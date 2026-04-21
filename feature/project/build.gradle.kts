plugins {
    id("pairshot.android.feature")
}

android {
    namespace = "com.pairshot.feature.project"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:infra"))
}
