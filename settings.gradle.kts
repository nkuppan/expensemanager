pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "expensemanager"

include(":app")
include(":data")
include(":core-common")
include(":core-model")
include(":core-ui")
include(":core-testing")
include(":feature-account")
include(":feature-category")
include(":feature-transaction")
include(":feature-dashboard")
include(":feature-settings")
include(":feature-analysis")
include(":macrobenchmark")
include(":microbenchmark")
