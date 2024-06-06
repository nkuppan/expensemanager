import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import java.io.FileInputStream
import java.util.Locale
import java.util.Properties

plugins {
    id("naveenapps.plugin.android.app")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
    id("com.github.triplet.play")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.appdistribution")
    id("com.google.android.gms.oss-licenses-plugin")
    id("jacoco")
}


val keysFolderPath: String = if (File("${rootDir.absolutePath}/keys").exists()) {
    "${rootDir.absolutePath}/keys"
} else {
    rootDir.absolutePath
}

fun getCredentialsFile(): File {
    val credentialFilePath = "$keysFolderPath/credentials.properties"
    return File(credentialFilePath)
}

fun getKeystoreFile(): File {
    val keystoreFilePath = "$keysFolderPath/android_keystore.jks"
    return File(keystoreFilePath)
}

fun getPlayStorePublisherFile(): File {
    val playStorePublisherFile = "$keysFolderPath/play_publish.json"
    return File(playStorePublisherFile)
}

fun getFirebasePublisherFile(): File {
    val firebasePublishJson = "$keysFolderPath/firebase_distribution_service_account.json"
    return File(firebasePublishJson)
}

val credentials = getCredentialsFile()
val keystore = getKeystoreFile()
if (credentials.exists() && keystore.exists()) {
    println("----- Both Keystore & Credentials available -----")
    println("----- ${credentials.absolutePath} -----")
    val properties = Properties().apply {
        load(FileInputStream(credentials))
    }

    android {
        signingConfigs {
            create("release") {
                keyAlias = properties.getProperty("KEY_ALIAS")
                storePassword = properties.getProperty("KEY_STORE_PASSWORD")
                keyPassword = properties.getProperty("KEY_PASSWORD")
                storeFile = keystore
            }
        }
    }
} else {
    println("----- Credentials not available -----")
}

val playStorePublisher = getPlayStorePublisherFile()
if (playStorePublisher.exists()) {
    println("----- Play Store Publisher available -----")
    println("----- ${playStorePublisher.absolutePath} -----")
    val track = System.getenv()["PLAYSTORE_TRACK"]
    val status = System.getenv()["PLAYSTORE_RELEASE_STATUS"]?.uppercase()
    println("----- ENV: $track & $status -----")
    val playStoreTrack = track ?: "beta"
    val playStoreReleaseStatus =
        runCatching { ReleaseStatus.valueOf(status!!) }.getOrNull() ?: ReleaseStatus.DRAFT

    println("----- $playStoreTrack & $playStoreReleaseStatus-----")

    android {
        play {
            this.serviceAccountCredentials.set(playStorePublisher)
            this.track.set(playStoreTrack)
            this.releaseStatus.set(playStoreReleaseStatus)
            println(this.serviceAccountCredentials.get().asFile.absolutePath)
        }
    }
} else {
    println("----- Publisher not available -----")
}

val firebasePublisher = getFirebasePublisherFile()
if (firebasePublisher.exists()) {
    println("----- Firebase Distribution Publisher available -----")
    println("----- ${firebasePublisher.absolutePath} -----")
    var firebaseDistGroups = System.getenv()["FIREBASE_DISTRIBUTION_GROUPS"]
    if (firebaseDistGroups.isNullOrBlank()) {
        firebaseDistGroups = "testers"
    }

    android {
        buildTypes {
            debug {
                firebaseAppDistribution {
                    serviceCredentialsFile = firebasePublisher.absolutePath
                    releaseNotes = "Nothing for now"
                    groups = firebaseDistGroups
                }
            }
            release {
                firebaseAppDistribution {
                    serviceCredentialsFile = firebasePublisher.absolutePath
                    releaseNotes = "Nothing for now"
                    groups = firebaseDistGroups
                }
            }
        }

        /**
         * Our APK path leads to our universal APK file - do guarantee that it's present we need to
         * make the distribution task depend on the universal APK assembly task.
         */
        applicationVariants.all { variant ->
            variant.outputs.forEach { output ->
                tasks.filter {
                    return@filter it.name.startsWith(
                        "appDistributionUpload${variant.name.toCapital()}"
                    )
                }.forEach {
                    it?.dependsOn("assemble${variant.name.toCapital()}")
                }
            }
            return@all true
        }
    }
} else {
    println("----- Firebase Distribution Publisher not available -----")
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

            val keyStore = runCatching { signingConfigs.getByName("release") }.getOrNull()
                ?: signingConfigs.getByName("debug")

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

fun String.toCapital(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    }
}
