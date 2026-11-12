plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.analysis"
}

dependencies {
    implementation(project(":feature:filter"))
    implementation(project(":feature:category"))
    implementation(project(":feature:transaction"))

    // Vico 3: the compose artifact now holds the whole chart API (there's no separate core);
    // compose-m3 maps the app's Material 3 colours onto the chart (rememberM3VicoTheme).
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
}