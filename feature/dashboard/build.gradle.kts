plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
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
    // Home's "Daily reminders are off" banner turns the reminder on and schedules it.
    implementation(project(":core:notification"))
    // Streak card flame icon (R8 strips the unused icons from release builds).
    implementation(libs.androidx.compose.material.iconsExtended)
}
