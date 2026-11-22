plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.di")
}

android {
    namespace = "com.naveenapps.expensemanager.core.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:repository"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)

    // JvmApiInSharedLayer: keeps this module free of JVM/Android-only APIs ahead of KMP.
    lintChecks(project(":lint-rules"))

    androidTestImplementation(project(":core:testing"))
    testImplementation(project(":core:testing"))
}