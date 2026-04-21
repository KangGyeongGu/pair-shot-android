plugins {
    `kotlin-dsl`
}

group = "com.pairshot.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.versions.ksp.get()}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "pairshot.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "pairshot.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("kotlinLibrary") {
            id = "pairshot.kotlin.library"
            implementationClass = "KotlinLibraryConventionPlugin"
        }
    }
}
