import com.nkuppan.expensemanager.buildsrc.Libs
import com.nkuppan.expensemanager.buildsrc.Versions

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("androidx.navigation.safeargs")
    id("dagger.hilt.android.plugin")
    id("com.nkuppan.expensemanager.app.jacoco")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    compileSdk = Versions.compileSdk

    defaultConfig {

        applicationId = "com.nkuppan.expensemanager"

        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk

        versionCode = Versions.versionCode
        versionName = Versions.versionName

        vectorDrawables {
            useSupportLibrary = true
        }

        testInstrumentationRunner = "com.nkuppan.expensemanager.utils.HiltTestRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        create("benchmark") {
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
    implementation(project(":core-ui"))
    implementation(project(":data"))
    implementation(project(":feature-dashboard"))

    implementation(Libs.playCoreUpdate)
    implementation(Libs.playCoreUpdateKtx)

    implementation(Libs.Google.Hilt.android)
    kapt(Libs.Google.Hilt.hiltCompiler)

    implementation(platform(Libs.Firebase.bom))
    implementation(Libs.Firebase.crashlytics)
    implementation(Libs.Firebase.analytics)

    //Android Testing Related Library
    kaptAndroidTest(Libs.Google.Hilt.hiltCompiler)
    androidTestImplementation(project(":core-testing"))
    testImplementation(project(":core-testing"))
}