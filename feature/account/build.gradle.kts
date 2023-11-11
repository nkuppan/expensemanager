plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.account"
}

dependencies {
    implementation(":core:common")
    implementation(":core:data")
    implementation(":core:model")
    implementation(":core:designsystem")

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:testing"))
}