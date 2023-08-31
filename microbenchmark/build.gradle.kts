plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("androidx.benchmark")
}

android {

    defaultConfig {
        minSdk = 23
    }

    testBuildType = "release"

    buildTypes {
        debug {
            // Since isDebuggable can"t be modified by gradle for library modules,
            // it must be done in a manifest - see src/androidTest/AndroidManifest.xml
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "benchmark-proguard-rules.pro"
            )
        }
        release {
            isDefault = true
        }
    }
}

dependencies {
    androidTestImplementation(project(":data"))
    androidTestImplementation(project(":core-model"))
    implementation(libs.room.ktx)
}