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
        testInstrumentationRunner = "com.nkuppan.expensemanager.utils.HiltTestRunner"
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
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-model"))
    implementation(project(":data"))
    implementation(project(":feature-dashboard"))
    implementation(project(":feature-category"))
    implementation(project(":feature-transaction"))
    implementation(libs.app.update.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
}