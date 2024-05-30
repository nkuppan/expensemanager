plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.export"
}

dependencies {
    implementation(project(":feature:account"))
    implementation(project(":feature:filter"))
}