plugins {
    id("naveenapps.plugin.android.feature")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.feature.about"
}

dependencies {
    // Licences are shown by our own screens (licenses/), reading the files the oss-licenses
    // Gradle plugin generates; Google's play-services-oss-licenses UI isn't used any more.
}
