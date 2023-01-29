package com.nkuppan.expensemanager.buildsrc

object Versions {
    const val compileSdk = 32
    const val minSdk = 21
    const val macroBenchmarkMinSdk = 23
    const val targetSdk = 32

    const val versionCode = 1
    const val versionName = "1.0.0"

    const val androidGradlePlugin = "7.3.1"
    const val ktlint = "0.42.1"
    const val glide = "4.12.0"
    const val playCoreUpdate = "2.0.0"

    object Kotlin {
        const val kotlin = "1.7.0"
        const val coroutines = "1.6.4"
    }

    object AndroidX {
        const val core = "1.8.0"
        const val coreTest = "2.1.0"
        const val activity = "1.5.0"
        const val fragment = "1.5.0"
        const val appCompat = "1.4.2"
        const val constraintLayout = "2.1.4"
        const val recyclerView = "1.2.1"
        const val swipeRefreshLayout = "1.1.0"
        const val lifecycle = "2.5.0"
        const val preference = "1.2.0"
        const val viewPager2 = "1.0.0"
        const val room = "2.4.2"
        const val navigation = "2.5.0"
        const val dataStore = "1.0.0"
        const val workManager = "2.7.1"

        object Compose {
            const val composeCompilerVersion = "1.2.0"
            const val compose = "1.2.0-rc03"
            const val activity = "1.5.0"
            const val poolingContainer = "1.0.0-rc01"
        }

        object Test {
            const val core = "1.4.0"
            const val runner = "1.4.0"
            const val rules = "1.4.0"
            const val jUnit = "1.1.3"
            const val espresso = "3.4.0"
            const val uiautomator = "2.2.0"
        }

        object Benchmark {
            const val macro = "1.2.0-alpha01"
            const val micro = "1.1.0"
        }

        object BaselineProfile {
            const val profileInstaller = "1.2.0"
        }
    }

    object Google {
        const val truth = "1.1.3"
        const val material = "1.6.0-alpha03"
        const val material3Compose = "1.0.0-alpha10"
        const val hilt = "2.42"
        const val hiltNavigationCompose = "1.0.0"

        object OssLicenses {
            const val ossLicenses = "17.0.0"
            const val gradlePlugin = "0.10.5"
        }

        object Play {
            const val core = "2.0.0"
        }
    }

    object Test {
        const val jUnitCore = "4.13.2"
        const val cashTurbine = "0.7.0"
    }

    object Mockito {
        const val core = "3.3.3"
        const val ktx = "4.0.0"
        const val android = "4.3.1"
    }

    object Jacoco {
        const val gradle = "0.8.7"
    }

    object Firebase {
        const val bom = "25.12.0"
    }

    object ThirdParty {
        const val mpcharts = "v3.1.0"
        const val colorpicker = "2.2.4"
    }
}