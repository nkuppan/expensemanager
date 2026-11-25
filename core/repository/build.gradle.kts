plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
}

android {
    namespace = "com.naveenapps.expensemanager.core.repository"

    // This module has no JVM/Android-only imports left (KMP groundwork C3), so the shared-layer
    // check is an error here: anything new must stay multiplatform-ready.
    lint {
        error += "JvmApiInSharedLayer"
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    implementation(libs.kotlinx.coroutines.android)

    // JvmApiInSharedLayer: keeps this module free of JVM/Android-only APIs ahead of KMP.
    lintChecks(project(":lint-rules"))
}