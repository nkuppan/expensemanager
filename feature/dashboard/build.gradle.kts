plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.dashboard"
}

dependencies {
    implementation(project(":feature:account"))
    implementation(project(":feature:budget"))
    implementation(project(":feature:category"))
    implementation(project(":feature:filter"))
    implementation(project(":feature:transaction"))
}
