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
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It's signed with a debug key
        // for easy local/CI testing.
        val benchmark by creating {
            // Keep the build type debuggable so we can attach a debugger if needed.
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(Libs.AndroidX.Test.JUnit.ktx)
    implementation(Libs.AndroidX.Test.Espresso.core)
    implementation(Libs.AndroidX.Test.UiAutomator.uiautomator)
    implementation(Libs.AndroidX.Benchmark.macroBenchmark)
    implementation("androidx.profileinstaller:profileinstaller:1.2.0-beta01")
}

androidComponents {
    beforeVariants {
        it.enable = it.buildType == "benchmark"
    }
}