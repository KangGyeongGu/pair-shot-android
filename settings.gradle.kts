pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "PairShot"

include(":app")

// :core modules
include(":core:model")
include(":core:domain")
include(":core:navigation")
include(":core:ui")
include(":core:designsystem")
include(":core:rendering")
include(":core:infra")
include(":core:database")
include(":core:storage")
include(":core:datastore")
include(":core:data")

// :feature modules
include(":feature:camera")
include(":feature:gallery")
include(":feature:compare")
include(":feature:export")
include(":feature:project")
include(":feature:settings")
