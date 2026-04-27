import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("pairshot.android.library")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            val composeMetricsDir =
                layout.buildDirectory
                    .dir("compose_metrics")
                    .map { it.asFile.absolutePath }

            tasks.withType<KotlinCompilationTask<*>>().configureEach {
                compilerOptions.freeCompilerArgs.addAll(
                    composeMetricsDir.map { dir ->
                        listOf(
                            "-P",
                            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$dir",
                            "-P",
                            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$dir",
                        )
                    },
                )
            }

            dependencies.apply {
                val composeBom = libs.findLibrary("compose-bom").get()
                add("implementation", platform(composeBom))
                add("implementation", libs.findLibrary("compose-ui").get())
                add("implementation", libs.findLibrary("compose-material3").get())
                add("implementation", libs.findLibrary("compose-tooling-preview").get())
                add("implementation", libs.findLibrary("compose-material-icons-extended").get())
                add("implementation", libs.findLibrary("activity-compose").get())

                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())

                add("implementation", libs.findLibrary("lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())

                add("implementation", libs.findLibrary("navigation-compose").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())

                add("implementation", libs.findLibrary("timber").get())

                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("coroutines-test").get())
                add("testImplementation", libs.findLibrary("turbine").get())
            }
        }
    }
}
