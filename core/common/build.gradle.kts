plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.hilt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.naveenapps.expensemanager.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.joda.time)
}