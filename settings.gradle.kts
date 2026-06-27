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
        maven { url = uri("https://jitpack.io") } // true-time 라이브러리 저장소
    }
}

rootProject.name = "Minary"
include(":app")
include(":core:common")
include(":core:di")
include(":core:ui:common")
include(":core:ui:design")
include(":core:storage")
include(":core:database")
include(":core:datastore")
include(":core:firebase")
include(":domain")
include(":data")
include(":presentation")
