plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("kotlin-parcelize")
}

android {
    namespace = "com.naveenapps.expensemanager.core.model"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.datetime)
}