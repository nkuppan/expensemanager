// Lists all plugins used throughout the project without applying them.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.navigation.safe) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.benchmark) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.play.publish) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
