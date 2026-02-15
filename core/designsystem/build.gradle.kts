plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.naveenapps.expensemanager.core.designsystem"
}

dependencies {
    implementation(project(":core:common"))
    api(libs.naveenapps.designsystem)
    api(libs.accompanist.permissions)
    debugApi(libs.androidx.compose.ui.tooling)
    api(libs.mpcharts)
}