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
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

rootProject.name = "expensemanager"

include(":app")
include(":macrobenchmark")

include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:navigation")
include(":core:notification")
include(":core:repository")
include(":core:testing")

include(":feature:account")
include(":feature:analysis")
include(":feature:budget")
include(":feature:category")
include(":feature:currency")
include(":feature:dashboard")
include(":feature:datefilter")
include(":feature:export")
include(":feature:onboarding")
include(":feature:reminder")
include(":feature:settings")
include(":feature:theme")
include(":feature:transaction")
