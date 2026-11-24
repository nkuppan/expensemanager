plugins {
    // Kotlin Multiplatform: Android + iOS (OpenSpec change make-core-domain-multiplatform).
    id("naveenapps.plugin.kmp.library")
}

// Use cases and pure business logic shared by every platform. No Android, no java.*: the iOS
// compile enforces it. Display text and colours are left to each platform's UI.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:repository"))
            implementation(project(":core:foundation"))
            implementation(libs.kotlinx.datetime)
            // Use-case modules (org.koin.dsl.module only: no annotations, no KSP).
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            // @Stable; published for iOS too.
            implementation(libs.androidx.compose.runtime.annotation)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        // JVM-only tests (Mockito, Truth, JUnit 4, fakes) from core:testing. Its Compose test
        // libraries take their versions from the Compose BOM, which core:testing only declares
        // as `implementation`, so this source set needs the BOM too.
        getByName("androidHostTest").dependencies {
            implementation(project(":core:testing"))
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
        }
    }
}
