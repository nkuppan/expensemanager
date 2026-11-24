plugins {
    // Kotlin Multiplatform: Android + iOS (OpenSpec change make-core-model-multiplatform).
    id("naveenapps.plugin.kmp.library")
}

// Domain models shared by every platform. No Android, no java.*: the iOS compile enforces it,
// so no shared-layer lint check is needed.
kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: model classes expose kotlinx-datetime types (LocalDate, ...) to every consumer.
            api(libs.kotlinx.datetime)
            // @Stable; published for iOS too.
            implementation(libs.androidx.compose.runtime.annotation)
        }
    }
}
