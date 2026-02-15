plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
}

android {
    namespace = "com.naveenapps.expensemanager.core.model"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:settings"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.compose.runtime.annotation)
}