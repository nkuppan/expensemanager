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

    implementation(libs.vico.compose)
    implementation(libs.vico.core)
}