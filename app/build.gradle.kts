plugins {
    id("nkuppan.plugin.android.app")
    id("nkuppan.plugin.kotlin.basic")
    id("nkuppan.plugin.compose")
    id("nkuppan.plugin.hilt")
    id("androidx.navigation.safeargs")
}

android {

    namespace = "com.nkuppan.expensemanager"

    defaultConfig {
        applicationId = "com.nkuppan.expensemanager"
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
}

dependencies {

    implementation(libs.colorpicker)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.navigation.ui)

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

    implementation(libs.mpcharts)
    implementation(libs.jxl)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    //Core testing source module it will hold the
    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))

    implementation(libs.app.update.ktx)
}