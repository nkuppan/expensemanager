import com.nkuppan.expensemanager.buildsrc.Libs
import com.nkuppan.expensemanager.buildsrc.Versions

plugins {
    id("nkuppan.plugin.android.library")
    id("nkuppan.plugin.kotlin.basic")
    id("dagger.hilt.android.plugin")
}


android {
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

    implementation(Libs.Google.Hilt.android)
    kapt(Libs.Google.Hilt.hiltCompiler)

    debugApi(Libs.AndroidX.Fragment.test)

    api(Libs.Google.truth)
    api(Libs.Mockito.core)
    api(Libs.Mockito.ktx)
    api(Libs.Kotlin.Coroutines.test)
    api(Libs.Google.Hilt.test)
    api(Libs.Test.cashTurbine)

    api(Libs.AndroidX.Core.test)
    api(Libs.AndroidX.Test.JUnit.ktx)
    api(Libs.AndroidX.Test.Espresso.core)
    api(Libs.AndroidX.Test.Espresso.contrib) {
        exclude(group = "org.checkerframework", module = "checker")
    }

    api(Libs.AndroidX.Test.runner)
    api(Libs.AndroidX.Test.JUnit.core)
}