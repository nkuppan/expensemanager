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
include(":macrobenchmark")

include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:model")
include(":core:notification")
include(":core:navigation")
include(":core:testing")

include(":feature:account")
include(":feature:analysis")
include(":feature:budget")
include(":feature:category")
include(":feature:currency")
include(":feature:datefilter")
include(":feature:export")
include(":feature:reminder")
include(":feature:settings")
include(":feature:transaction")
include(":core:navigation")
include(":core:common")
include(":core:domain")
include(":feature:dashboard")
include(":feature:theme")
include(":feature:onboarding")
