/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
    id("naveenapps.plugin.android.library")
    id("naveenapps.plugin.kotlin.basic")
    id("naveenapps.plugin.compose")
    id("naveenapps.plugin.hilt")
}

android {
    namespace = "com.google.samples.apps.nowinandroid.core.testing"
}

dependencies {
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.androidx.test.espresso.core)
    api(libs.androidx.test.uiautomator)
    api(libs.androidx.test.ext)
    api(libs.androidx.test.core.arch)
    api(libs.androidx.test.core)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.runner)
    api(libs.mockito)
    api(libs.truth)

    debugApi(libs.androidx.compose.ui.testManifest)

    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    //implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:notification"))
    implementation(libs.kotlinx.datetime)
}
