import com.nkuppan.expensemanager.buildsrc.Libs
import com.nkuppan.expensemanager.buildsrc.Versions

plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("dagger.hilt.android.plugin")
}

android {
    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.AndroidX.Compose.composeCompilerVersion
    }
}

dependencies {
    api(Libs.Kotlin.stdlib)
    api(Libs.Kotlin.Coroutines.core)
    api(Libs.Kotlin.Coroutines.android)

    api(Libs.AndroidX.Core.ktx)
    api(Libs.AndroidX.appCompat)
    api(Libs.AndroidX.constraintLayout)
    api(Libs.AndroidX.recyclerView)
    api(Libs.AndroidX.swipeRefreshLayout)

    api(Libs.AndroidX.Activity.ktx)
    api(Libs.AndroidX.Fragment.ktx)

    api(Libs.AndroidX.Lifecycle.liveDataKtx)
    api(Libs.AndroidX.Lifecycle.viewModelKtx)
    api(Libs.AndroidX.Lifecycle.compose)
    api(Libs.AndroidX.Lifecycle.runtime)

    api(Libs.AndroidX.Navigation.fragmentKtx)
    api(Libs.AndroidX.Navigation.uiKtx)
    api(Libs.AndroidX.Navigation.compose)

    api(Libs.AndroidX.Compose.activity)

    api(Libs.AndroidX.Compose.runtime)
    api(Libs.AndroidX.Compose.animation)
    api(Libs.AndroidX.Compose.foundation)
    api(Libs.AndroidX.Compose.foundationLayout)
    api(Libs.AndroidX.Compose.viewBinding)
    api(Libs.AndroidX.Compose.ui)
    api(Libs.AndroidX.Compose.uiUtils)
    api(Libs.AndroidX.Compose.uiText)
    api(Libs.AndroidX.Compose.uiToolingPreview)
    api(Libs.AndroidX.Compose.material)
    api(Libs.AndroidX.Compose.materialIcon)
    api(Libs.Google.Hilt.hiltNavigationCompose)

    debugApi(Libs.AndroidX.Compose.uiTooling)
    debugApi(Libs.AndroidX.Compose.poolingContainer)
    debugApi(Libs.AndroidX.Lifecycle.compose)

    api(Libs.Google.material)
    api(Libs.Google.material3Compose)

    api(Libs.AndroidX.DataStore.preferences)

    implementation(Libs.Glide.core)
    kapt(Libs.Glide.compiler)

    implementation(Libs.Google.Hilt.android)
    kapt(Libs.Google.Hilt.hiltCompiler)
}