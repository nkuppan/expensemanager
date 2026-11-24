plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.core.common"
}

dependencies {
    // Pure Kotlin utilities that moved out of this module (KMP: add-kmp-foundation-module).
    api(project(":core:foundation"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // DateBridgesTest (KMP groundwork C2).
    testImplementation(libs.junit4)
}