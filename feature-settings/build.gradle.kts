import com.nkuppan.expensemanager.buildsrc.Libs
import com.nkuppan.expensemanager.buildsrc.Versions

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("androidx.navigation.safeargs")
    id("dagger.hilt.android.plugin")
    id("com.nkuppan.expensemanager.library.jacoco")
}

android {
    compileSdk = Versions.compileSdk

    defaultConfig {
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(fileTree("libs"))

    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":data"))
    implementation(project(":core-model"))

    implementation(Libs.Google.OssLicenses.ossLicenses)

    implementation(Libs.Google.Hilt.android)
    kapt(Libs.Google.Hilt.hiltCompiler)

    implementation(Libs.Google.Play.review)
    implementation(Libs.AndroidX.preference)

    implementation(Libs.AndroidX.WorkManager.runtimeKtx)

    //Android Testing Related Library
    kaptAndroidTest(Libs.Google.Hilt.hiltCompiler)
    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}