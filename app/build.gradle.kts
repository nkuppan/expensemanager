plugins {
    id("naveenapps.plugin.android.app")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
    id("androidx.navigation.safeargs")
    id("kotlin-parcelize")
}

android {

    namespace = "com.naveenapps.expensemanager"

    defaultConfig {
        applicationId = "com.naveenapps.expensemanager"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        create("macrobenchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
    }

    testOptions {
        managedDevices {
            devices {
                maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 2"
                    // Use only API levels 27 and higher.
                    apiLevel = 30
                    // To include Google services, use "google".
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    implementation(project(":feature:account"))
    implementation(project(":feature:analysis"))
    implementation(project(":feature:budget"))
    implementation(project(":feature:category"))
    implementation(project(":feature:dashboard"))

    implementation(project(":feature:settings"))
    implementation(project(":feature:export"))
    implementation(project(":feature:datefilter"))
    implementation(project(":feature:reminder"))
    implementation(project(":feature:currency"))
    implementation(project(":feature:theme"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.profileinstaller)

    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.metrics)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemUIController)

    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preference)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.ktx)
    implementation(libs.google.android.play.review)
    implementation(libs.app.update.ktx)
    implementation(libs.opencsv)

    implementation(libs.jxl)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.core)
    implementation(libs.vico.views)
    implementation(libs.joda.time)

    testImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:testing"))
}