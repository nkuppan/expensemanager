import com.nkuppan.expensemanager.buildsrc.Libs
import com.nkuppan.expensemanager.buildsrc.Versions

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.nkuppan.expensemanager.library.jacoco")
}

android {

    compileSdk = Versions.compileSdk

    defaultConfig {

        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk

        testInstrumentationRunner = Libs.AndroidX.Test.instrumentationRunner
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packagingOptions {
        resources {
            excludes.add("**/attach_hotspot_windows.dll")
            excludes.add("META-INF/licenses/**")
            excludes.add("META-INF/AL2.0")
            excludes.add("META-INF/LGPL2.1")
        }
    }
}

dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-model"))

    implementation(Libs.AndroidX.appCompat)

    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.Kotlin.Coroutines.core)
    implementation(Libs.Kotlin.Coroutines.android)

    implementation(Libs.AndroidX.Room.runtime)
    kapt(Libs.AndroidX.Room.compiler)
    implementation(Libs.AndroidX.Room.ktx)

    implementation(Libs.Glide.core)
    kapt(Libs.Glide.compiler)

    implementation(Libs.Google.Hilt.android)
    kapt(Libs.Google.Hilt.hiltCompiler)

    implementation(Libs.AndroidX.DataStore.preferences)

    implementation("androidx.startup:startup-runtime:1.1.1")

    //Core testing source module it will hold the
    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}