plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.naveenapps.expensemanager.core.designsystem"
}

dependencies {
    implementation(project(":core:common"))

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
    api(libs.accompanist.permissions)
    api(libs.accompanist.systemUIController)

    debugApi(libs.androidx.compose.ui.tooling)

    api(libs.androidx.core.ktx)

    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)
    implementation(libs.vico.views)

    implementation(libs.mpcharts)
}