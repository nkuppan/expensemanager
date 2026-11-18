package com.naveenapps.expensemanager.core.data.repository

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.naveenapps.expensemanager.core.repository.AnalyticsPrivacy
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository

class AnalyticsRepositoryImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsRepository {

    override fun trackAppOpenEvent() {
        setUserProperties()
        logEvent(EVENT_NAME_APP_OPEN, mapOf())
    }

    override fun logEvent(eventName: String, params: Map<String, String>) {
        // Safety net: money-like parameters never leave the device (see AnalyticsPrivacy).
        val allowed = AnalyticsPrivacy.filter(params)
        if (allowed.size != params.size) {
            Log.w(TAG, "Dropped money-like params from $eventName: ${params.keys - allowed.keys}")
        }
        firebaseAnalytics.logEvent(
            /* name = */ eventName,
            /* params = */ Bundle().apply {
                allowed.onEach {
                    putString(it.key, it.value)
                }
            },
        )
    }

    override fun setCurrentScreen(screenName: String) {
        // Firebase's standard screen_view event, so screens show up in the built-in
        // "Pages and screens" report (single-Activity apps get no automatic per-screen data).
        firebaseAnalytics.logEvent(
            /* name = */ FirebaseAnalytics.Event.SCREEN_VIEW,
            /* params = */ Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            },
        )
    }

    override fun setUserProperties() {
        // Device name, brand and OS version are recorded by Analytics already (Device model,
        // Brand, OS version), so these copies only used up user-property slots. Cleared once so
        // old values stop being attached; behaviour properties are set by UserPropertiesTracker.
        listOf(DEVICE_NAME, DEVICE_BRAND, DEVICE_OS_NAME, DEVICE_OS_NUMBER).forEach {
            firebaseAnalytics.setUserProperty(it, null)
        }
    }

    override fun setUserProperty(name: String, value: String?) {
        if (AnalyticsPrivacy.isBlocked(name)) {
            Log.w(TAG, "Dropped money-like user property $name")
            return
        }
        firebaseAnalytics.setUserProperty(name, value?.take(MAX_USER_PROPERTY_VALUE))
    }

    companion object {
        private const val EVENT_NAME_APP_OPEN = "app_open"

        private const val DEVICE_OS_NAME = "device_os_name"
        private const val DEVICE_NAME = "device_name"
        private const val DEVICE_BRAND = "device_brand"
        private const val DEVICE_OS_NUMBER = "device_os_number"
        private const val MAX_USER_PROPERTY_VALUE = 36
        private const val TAG = "Analytics"
    }
}
