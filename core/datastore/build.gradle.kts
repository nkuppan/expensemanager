plugins {
    // Kotlin Multiplatform: Android + iOS (OpenSpec change make-core-datastore-multiplatform).
    id("naveenapps.plugin.kmp.library")
    kotlin("plugin.serialization")
}

// Settings stores shared by every platform. The store classes are commonMain; each platform only
// decides where the files live: androidMain keeps the Context.preferencesDataStore delegates
// (same files as always, see the backup rules in :app), iosMain creates them under Application
// Support/datastore/.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:foundation"))
            implementation(libs.androidx.dataStore.preference.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("androidMain").dependencies {
            // Context.preferencesDataStore and Koin's androidContext().
            implementation(libs.androidx.dataStore.preference)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.android)
        }
        // JVM-only tests: the real org.json for InsightJsonCompatibilityTest (Android's built-in
        // one is a stub in JVM tests).
        getByName("androidHostTest").dependencies {
            implementation(libs.org.json)
            implementation(libs.junit4)
        }
    }
}
