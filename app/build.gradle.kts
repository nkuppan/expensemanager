import java.io.FileInputStream
import java.util.Properties

plugins {
    id("naveenapps.plugin.android.app")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
    id("androidx.navigation.safeargs")
    id("com.github.triplet.play")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.gms.oss-licenses-plugin")
    id("jacoco")
}

val credentialFilePath = "${rootDir.absolutePath}/keys/credentials.properties"
println(credentialFilePath)
val credentials = File(credentialFilePath)
if (credentials.exists()) {
    println("----- Credentials available -----")
    val properties = Properties().apply {
        load(FileInputStream(credentials))
    }

    android {
        signingConfigs {
            create("release") {
                keyAlias = properties.getProperty("KEY_ALIAS")
                storePassword = properties.getProperty("KEY_STORE_PASSWORD")
                keyPassword = properties.getProperty("KEY_PASSWORD")
                storeFile = File("${rootDir.absolutePath}/keys/android_keystore.jks")
                println("Credentials release created")
            }
        }
    }
} else {
    println("----- Credentials not available -----")
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
            applicationIdSuffix = ".debug"
            enableUnitTestCoverage = true
        }
        create("macrobenchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val keyStore = runCatching { signingConfigs.getByName("release") }.getOrNull() ?:
                signingConfigs.getByName("debug")

            signingConfig = keyStore
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
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

    play {
        serviceAccountCredentials.set(file("../keys/play_publish.json"))
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:navigation"))
    implementation(project(":core:notification"))
    implementation(project(":core:repository"))

    implementation(project(":feature:account"))
    implementation(project(":feature:analysis"))
    implementation(project(":feature:budget"))
    implementation(project(":feature:category"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:transaction"))
    implementation(project(":feature:onboarding"))

    implementation(project(":feature:settings"))
    implementation(project(":feature:theme"))
    implementation(project(":feature:export"))
    implementation(project(":feature:reminder"))
    implementation(project(":feature:currency"))
    implementation(project(":feature:about"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    implementation(libs.androidx.splash.screen)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.profileinstaller)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.ext.work)

    implementation(libs.google.oss.licenses)

    implementation(libs.app.update.ktx)

    testImplementation(project(":core:testing"))
    androidTestImplementation(project(":core:testing"))
}