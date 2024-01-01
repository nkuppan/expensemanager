plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.core.testing"
}

dependencies {
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.androidx.test.espresso.core)
    api(libs.androidx.test.uiautomator)
    api(libs.androidx.test.ext)
    api(libs.androidx.test.core.arch)
    api(libs.androidx.test.core)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.runner)
    api(libs.mockito)
    api(libs.truth)
    api(libs.robolectric)

    implementation(project(":core:model"))
    implementation(libs.kotlinx.datetime)
}
