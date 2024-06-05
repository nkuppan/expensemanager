plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.onboarding"
}

dependencies {
    implementation(project(":feature:country"))
    implementation(project(":feature:currency"))
    implementation(project(":feature:account"))
    implementation(libs.lottie.compose)
}