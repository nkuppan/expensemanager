plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.core.settings"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.dataStore.preference)
}