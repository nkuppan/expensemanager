package com.naveenapps.expensemanager.core.data.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val AppModule = module {
    // Unknown keys are ignored, as Gson did.
    single { Json { ignoreUnknownKeys = true } }
    single<FirebaseRemoteConfig> { Firebase.remoteConfig }
    single<FirebaseAnalytics> { Firebase.analytics }
    single<FirebaseCrashlytics> { Firebase.crashlytics }
}
