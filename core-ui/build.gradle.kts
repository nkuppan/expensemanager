plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.compose")
    id("nkuppan.plugin.hilt")
}

android {
    namespace = "com.nkuppan.expensemanager.core.ui"
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    api(libs.colorpicker)

    api(libs.androidx.appcompat)
    api(libs.androidx.material)
    api(libs.androidx.navigation)
    api(libs.androidx.navigation.ui)

    api(libs.androidx.compose.activity)
    api(libs.androidx.compose.animation)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.foundation.layout)
    api(libs.androidx.compose.material.iconsExtended)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.runtime.livedata)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.ui.util)
    api(libs.androidx.metrics)
    api(libs.androidx.tracing.ktx)
    api(libs.androidx.hilt.navigation.compose)

    debugApi(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    implementation(libs.kotlinx.datetime)
}