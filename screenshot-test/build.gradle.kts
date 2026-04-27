plugins {
    id("pairshot.android.library")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.pairshot.screenshot"
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty(
                    "roborazzi.test.record",
                    project.findProperty("roborazzi.test.record") ?: "false",
                )
                it.systemProperty(
                    "roborazzi.test.verify",
                    project.findProperty("roborazzi.test.verify") ?: "false",
                )
                it.systemProperty(
                    "roborazzi.test.compare",
                    project.findProperty("roborazzi.test.compare") ?: "false",
                )
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:infra"))
    implementation(project(":feature:home"))
    implementation(project(":feature:pair-preview"))
    implementation(project(":feature:album"))
    implementation(project(":feature:camera"))
    implementation(libs.camerax.core)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    testImplementation(composeBom)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
}
