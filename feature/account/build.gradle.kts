plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.account"

    testOptions {
        unitTests {
            // For Robolectric
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    implementation(project(":core:settings"))

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.espresso.core)
    debugImplementation(libs.androidx.compose.ui.testManifest)
}
