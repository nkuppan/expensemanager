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
    // TransactionItem (shared transaction row) takes Amount and TransactionType.
    implementation(project(":core:model"))
    api(libs.naveenapps.designsystem)
    api(libs.accompanist.permissions)
    debugApi(libs.androidx.compose.ui.tooling)
    // Category donut (PieChartView); Vico replaced MPAndroidChart.
    implementation(libs.vico.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
}