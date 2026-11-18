plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.naveenapps.expensemanager.core.designsystem"

    // Robolectric screenshot tests (Roborazzi) need the merged resources.
    testOptions.unitTests.isIncludeAndroidResources = true
}

// Screenshot tests of the design system gallery (ComponentGalleryScreenshotTest):
//   record:  ./gradlew :core:designsystem:testDebugUnitTest -Proborazzi.test.record=true
//   verify:  ./gradlew :core:designsystem:testDebugUnitTest -Proborazzi.test.verify=true
// Images live in core/designsystem/src/test/screenshots and are committed.
val roborazziModes = listOf("roborazzi.test.record", "roborazzi.test.verify", "roborazzi.test.compare")
    .associateWith { providers.gradleProperty(it) }

tasks.withType<Test>().configureEach {
    roborazziModes.forEach { (key, value) -> value.orNull?.let { systemProperty(key, it) } }
}

dependencies {
    implementation(project(":core:common"))
    // TransactionItem (shared transaction row) takes Amount and TransactionType.
    implementation(project(":core:model"))
    api(libs.naveenapps.designsystem)
    api(libs.accompanist.permissions)
    debugApi(libs.androidx.compose.ui.tooling)
    // Category donut (PieChartView); Vico replaced MPAndroidChart.
    implementation(libs.vico.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.iconsExtended)

    testImplementation(project(":core:testing"))
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}