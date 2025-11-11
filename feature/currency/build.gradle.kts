plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.currency"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(project(":feature:country"))
    implementation(project(":core:settings"))
}