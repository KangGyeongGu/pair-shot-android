import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    if (name != "convention") {
        apply(plugin = "io.gitlab.arturbosch.detekt")

        extensions.configure<DetektExtension> {
            toolVersion = "1.23.7"
            source.setFrom(
                "src/main/java",
                "src/main/kotlin",
                "src/test/java",
                "src/test/kotlin",
            )
            config.setFrom(rootProject.files("detekt.yml"))
            buildUponDefaultConfig = true
            autoCorrect = false
            parallel = true
        }

        tasks.withType<Detekt>().configureEach {
            jvmTarget = "17"
            reports {
                html.required.set(true)
                xml.required.set(false)
                txt.required.set(false)
                md.required.set(false)
            }
        }
    }
}
