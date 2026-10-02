package com.naveenapps.expensemanager.core.data.repository

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.DevicePropertyRepository

class AnalyticsRepositoryImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val devicePropertyRepository: DevicePropertyRepository,
) : AnalyticsRepository {

    override fun trackAppOpenEvent() {
        setUserProperties()
        logEvent(EVENT_NAME_APP_OPEN, mapOf())
    }

    override fun logEvent(eventName: String, params: Map<String, String>) {
        firebaseAnalytics.logEvent(
            /* name = */ eventName,
            /* params = */ Bundle().apply {
                params.onEach {
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
        firebaseAnalytics.setUserProperty(
            DEVICE_NAME,
            devicePropertyRepository.getDeviceName(),
        )
        firebaseAnalytics.setUserProperty(
            DEVICE_BRAND,
            devicePropertyRepository.getDeviceBrandName(),
        )
        firebaseAnalytics.setUserProperty(
            DEVICE_OS_NAME,
            devicePropertyRepository.getDeviceOsVersion(),
        )
        firebaseAnalytics.setUserProperty(
            DEVICE_OS_NUMBER,
            devicePropertyRepository.getDeviceOsVersionNumber(),
        )
    }

    companion object {
        private const val EVENT_NAME_APP_OPEN = "app_open"

        private const val DEVICE_OS_NAME = "device_os_name"
        private const val DEVICE_NAME = "device_name"
        private const val DEVICE_BRAND = "device_brand"
        private const val DEVICE_OS_NUMBER = "device_os_number"
    }
}
