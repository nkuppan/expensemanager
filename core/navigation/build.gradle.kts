plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.naveenapps.expensemanager.core.navigation"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":core:designsystem"))
}