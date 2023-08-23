import com.nkuppan.expensemanager.buildsrc.Versions
import com.nkuppan.expensemanager.buildsrc.Libs

plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nkuppan.expensemanager.benchmark"
    compileSdk = Versions.compileSdk

    defaultConfig {
        minSdk = Versions.macroBenchmarkMinSdk
        targetSdk = Versions.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It's signed with a debug key
        // for easy local/CI testing.
        val macrobenchmark by creating {
            // Keep the build type debuggable so we can attach a debugger if needed.
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    testOptions {
        managedDevices {
            devices {
                create("pixel2Api31", com.android.build.api.dsl.ManagedVirtualDevice::class) {
                    device = "Pixel 2"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
            }
        }
    }

    targetProjectPath = ":app"

    experimentalProperties["android.experimental.self-instrumenting"] = true

    defaultConfig {
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "DEBUGGABLE"
    }
}

dependencies {
    implementation(Libs.AndroidX.Test.JUnit.ktx)
    implementation(Libs.AndroidX.Test.Espresso.core)
    implementation(Libs.AndroidX.Test.UiAutomator.uiautomator)
    implementation(Libs.AndroidX.Benchmark.macroBenchmark)
    implementation(Libs.AndroidX.BaselineProfile.profileInstaller)
}

androidComponents {
/*
    beforeVariants {
        it.enable = it.buildType == "macrobenchmark"
    }
*/
}