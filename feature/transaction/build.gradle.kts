plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.transaction"
}

dependencies {
    implementation(project(":core:settings"))

    implementation(project(":feature:category"))
    implementation(project(":feature:account"))
    implementation(project(":feature:filter"))

    // On-device OCR for shared receipt screenshots (bundled model, works offline).
    implementation(libs.google.mlkit.text.recognition)
    // Receipt camera: Google's scanner UI (crop, clean-up); no CAMERA permission needed.
    implementation(libs.google.mlkit.document.scanner)
}