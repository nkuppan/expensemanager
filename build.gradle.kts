// Lists all plugins used throughout the project without applying them.
buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.google.oss.licenses.plugin)
        classpath(libs.org.jacoco.core)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.benchmark) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.play.publish) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.distribution) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.dependency.analysis) apply false
}