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

    // On-device OCR for receipts. The model comes from Google Play services (shared, ~260 KB in
    // the app instead of ~11 MB of bundled native code); it still runs on the phone, offline.
    implementation(libs.google.mlkit.text.recognition)
    // Receipt camera: Google's scanner UI (crop, clean-up); no CAMERA permission needed.
    implementation(libs.google.mlkit.document.scanner)
    // Checks Play services and downloads the scanner module before opening it (ReceiptScanner.kt).
    implementation(libs.google.play.services.base)
}