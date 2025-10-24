package com.naveenapps.expensemanager.core.data.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.koin.dsl.module

val AppModule = module {
    single<Gson> { GsonBuilder().create() }
    single<FirebaseRemoteConfig> { Firebase.remoteConfig }
    single<FirebaseAnalytics> { Firebase.analytics }
}

