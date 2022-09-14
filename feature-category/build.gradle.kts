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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.AndroidX.Compose.composeCompilerVersion
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {

        jvmTarget = JavaVersion.VERSION_11.toString()

        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn",
            // Enable experimental coroutines APIs, including Flow
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlin.Experimental",
            // Enable experimental kotlinx serialization APIs
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":data"))
    implementation(project(":core-model"))

    implementation(Libs.ThirdParty.colorpicker)

    implementation(Libs.Google.Hilt.android)
    kapt(Libs.Google.Hilt.hiltCompiler)

    //Android Testing Related Library
    kaptAndroidTest(Libs.Google.Hilt.hiltCompiler)
    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}