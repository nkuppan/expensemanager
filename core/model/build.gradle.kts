plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("kotlin-parcelize")
}

android {
    namespace = "com.naveenapps.expensemanager.core.data"
}

dependencies {
    implementation(libs.kotlinx.datetime)
}