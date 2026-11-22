plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
}

android {
    namespace = "com.naveenapps.expensemanager.core.repository"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    implementation(libs.kotlinx.coroutines.android)

    // JvmApiInSharedLayer: keeps this module free of JVM/Android-only APIs ahead of KMP.
    lintChecks(project(":lint-rules"))
}