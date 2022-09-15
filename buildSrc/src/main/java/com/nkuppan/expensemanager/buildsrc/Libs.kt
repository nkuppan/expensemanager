package com.nkuppan.expensemanager.buildsrc

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    const val ktlint = "com.pinterest:ktlint:${Versions.ktlint}"

    const val playCoreUpdate = "com.google.android.play:app-update:${Versions.playCoreUpdate}"
    const val playCoreUpdateKtx =
        "com.google.android.play:app-update-ktx:${Versions.playCoreUpdate}"

    object Glide {
        const val core = "com.github.bumptech.glide:glide:${Versions.glide}"
        const val compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.Kotlin.kotlin}"
        const val gradlePlugin =
            "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Kotlin.kotlin}"

        object Coroutines {
            const val core =
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.Kotlin.coroutines}"
            const val android =
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"
            const val test =
                "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.coroutines}"
        }
    }

    object AndroidX {
        const val appCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appCompat}"
        const val constraintLayout =
            "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintLayout}"
        const val recyclerView =
            "androidx.recyclerview:recyclerview:${Versions.AndroidX.recyclerView}"
        const val swipeRefreshLayout =
            "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.AndroidX.swipeRefreshLayout}"
        const val preference = "androidx.preference:preference-ktx:${Versions.AndroidX.preference}"
        const val viewPager2 = "androidx.viewpager2:viewpager2:${Versions.AndroidX.viewPager2}"

        object Core {
            const val ktx = "androidx.core:core-ktx:${Versions.AndroidX.core}"
            const val test = "androidx.arch.core:core-testing:${Versions.AndroidX.coreTest}"
        }

        object Activity {
            const val ktx = "androidx.activity:activity-ktx:${Versions.AndroidX.activity}"
        }

        object Fragment {
            const val ktx = "androidx.fragment:fragment-ktx:${Versions.AndroidX.fragment}"
            const val test = "androidx.fragment:fragment-testing:${Versions.AndroidX.fragment}"
        }

        object Lifecycle {
            const val viewModelKtx =
                "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.lifecycle}"
            const val liveDataKtx =
                "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.lifecycle}"
            const val compose =
                "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.AndroidX.lifecycle}"
            const val runtime =
                "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.lifecycle}"
        }

        object Room {
            const val runtime = "androidx.room:room-runtime:${Versions.AndroidX.room}"
            const val compiler = "androidx.room:room-compiler:${Versions.AndroidX.room}"
            const val ktx = "androidx.room:room-ktx:${Versions.AndroidX.room}"
            const val testing = "androidx.room:room-testing:${Versions.AndroidX.room}"
        }

        object Navigation {
            const val uiKtx =
                "androidx.navigation:navigation-ui-ktx:${Versions.AndroidX.navigation}"
            const val fragmentKtx =
                "androidx.navigation:navigation-fragment-ktx:${Versions.AndroidX.navigation}"
            const val compose =
                "androidx.navigation:navigation-compose:${Versions.AndroidX.navigation}"
            const val safeArgsGradlePlugin =
                "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.AndroidX.navigation}"

            const val test =
                "androidx.navigation:navigation-testing:${Versions.AndroidX.navigation}"
        }

        object DataStore {
            const val preferences =
                "androidx.datastore:datastore-preferences:${Versions.AndroidX.dataStore}"
        }

        object WorkManager {
            const val runtimeKtx =
                "androidx.work:work-runtime-ktx:${Versions.AndroidX.workManager}"
        }

        object Test {
            const val runner = "androidx.test:runner:${Versions.AndroidX.Test.runner}"
            const val rules = "androidx.test:rules:${Versions.AndroidX.Test.rules}"
            const val instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            object Core {
                const val ktx = "androidx.test:core-ktx:${Versions.AndroidX.Test.core}"
            }

            object JUnit {
                const val core = "junit:junit:${Versions.AndroidX.Test.jUnit}"
                const val ktx = "androidx.test.ext:junit-ktx:${Versions.AndroidX.Test.jUnit}"
            }

            object Espresso {
                const val core =
                    "androidx.test.espresso:espresso-core:${Versions.AndroidX.Test.espresso}"
                const val contrib =
                    "androidx.test.espresso:espresso-contrib:${Versions.AndroidX.Test.espresso}"
            }

            object UiAutomator {
                const val uiautomator =
                    "androidx.test.uiautomator:uiautomator:${Versions.AndroidX.Test.uiautomator}"
            }
        }

        object Benchmark {
            const val macroBenchmark =
                "androidx.benchmark:benchmark-macro-junit4:${Versions.AndroidX.Benchmark.macro}"
            const val microBenchmark =
                "androidx.benchmark:benchmark-junit4:${Versions.AndroidX.Benchmark.macro}"
            const val benchmarkGradlePlugin =
                "androidx.benchmark:benchmark-gradle-plugin:${Versions.AndroidX.Benchmark.micro}"
        }

        object BaselineProfile {
            const val profileInstaller =
                "androidx.profileinstaller:profileinstaller:${Versions.AndroidX.BaselineProfile.profileInstaller}"
        }

        object Compose {
            const val activity =
                "androidx.activity:activity-compose:${Versions.AndroidX.Compose.activity}"
            const val poolingContainer =
                "androidx.customview:customview-poolingcontainer:${Versions.AndroidX.Compose.poolingContainer}"
            const val runtime =
                "androidx.compose.runtime:runtime:${Versions.AndroidX.Compose.compose}"
            const val animation =
                "androidx.compose.animation:animation:${Versions.AndroidX.Compose.compose}"
            const val foundation =
                "androidx.compose.foundation:foundation:${Versions.AndroidX.Compose.compose}"
            const val foundationLayout =
                "androidx.compose.foundation:foundation-layout:${Versions.AndroidX.Compose.compose}"
            const val viewBinding =
                "androidx.compose.ui:ui-viewbinding:${Versions.AndroidX.Compose.compose}"
            const val ui = "androidx.compose.ui:ui-util:${Versions.AndroidX.Compose.compose}"
            const val uiUtils = "androidx.compose.ui:ui:${Versions.AndroidX.Compose.compose}"
            const val uiText =
                "androidx.compose.ui:ui-text-google-fonts:${Versions.AndroidX.Compose.compose}"
            const val uiTooling =
                "androidx.compose.ui:ui-tooling:${Versions.AndroidX.Compose.compose}"
            const val uiToolingPreview =
                "androidx.compose.ui:ui-tooling-preview:${Versions.AndroidX.Compose.compose}"
            const val material =
                "androidx.compose.material:material:${Versions.AndroidX.Compose.compose}"
            const val materialIcon =
                "androidx.compose.material:material-icons-extended:${Versions.AndroidX.Compose.compose}"
        }
    }

    object Google {
        const val material = "com.google.android.material:material:${Versions.Google.material}"
        const val material3Compose =
            "androidx.compose.material3:material3:${Versions.Google.material3Compose}"
        const val truth = "com.google.truth:truth:${Versions.Google.truth}"

        object Play {
            const val review = "com.google.android.play:review-ktx:${Versions.Google.Play.core}"
        }

        object Hilt {
            const val android = "com.google.dagger:hilt-android:${Versions.Google.hilt}"
            const val hiltCompiler =
                "com.google.dagger:hilt-android-compiler:${Versions.Google.hilt}"
            const val androidGradlePlugin =
                "com.google.dagger:hilt-android-gradle-plugin:${Versions.Google.hilt}"
            const val hiltNavigationCompose =
                "androidx.hilt:hilt-navigation-compose:${Versions.Google.hiltNavigationCompose}"
            const val test = "com.google.dagger:hilt-android-testing:${Versions.Google.hilt}"
        }

        object OssLicenses {
            const val ossLicenses =
                "com.google.android.gms:play-services-oss-licenses:${Versions.Google.OssLicenses.ossLicenses}"
            const val gradlePlugin =
                "com.google.android.gms:oss-licenses-plugin:${Versions.Google.OssLicenses.gradlePlugin}"
        }
    }

    object Mockito {
        const val core = "org.mockito:mockito-core:${Versions.Mockito.core}"
        const val ktx = "org.mockito.kotlin:mockito-kotlin:${Versions.Mockito.ktx}"
        const val android = "org.mockito:mockito-android:${Versions.Mockito.android}"
    }

    object Test {
        const val junit = "junit:junit:${Versions.Test.jUnitCore}"
        const val cashTurbine = "app.cash.turbine:turbine:${Versions.Test.cashTurbine}"
    }

    object Jacoco {
        const val gradle = "org.jacoco:org.jacoco.core:${Versions.Jacoco.gradle}"
    }

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:${Versions.Firebase.bom}"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
    }

    object ThirdParty {
        const val mpcharts = "com.github.PhilJay:MPAndroidChart:${Versions.ThirdParty.mpcharts}"
        const val colorpicker =
            "com.github.skydoves:colorpickerview:${Versions.ThirdParty.colorpicker}"
    }
}

