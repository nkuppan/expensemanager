package com.naveenapps.expensemanager.core.data.repository

import android.os.Build
import com.naveenapps.expensemanager.core.repository.DevicePropertyRepository
import java.util.Locale

class DevicePropertyRepositoryImpl() : DevicePropertyRepository {

    override fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        val model = Build.MODEL.lowercase(Locale.getDefault())
        return if (model.startsWith(manufacturer)) {
            Build.MODEL
        } else {
            "${Build.MANUFACTURER} ${Build.MODEL}"
        }
    }

    override fun getDeviceBrandName(): String {
        return Build.BRAND
    }

    override fun getDeviceOsVersion(): String {
        return getOsVersionName()
    }

    override fun getDeviceOsVersionNumber(): String {
        return Build.VERSION.SDK_INT.toString()
    }

    private fun getOsVersionName(): String {
        return when (val sdkInt = Build.VERSION.SDK_INT) {
            34 -> "Upside Down Cake"
            33 -> "Tiramisu"
            32 -> "Snow Cone v2"
            31 -> "Snow Cone"
            30 -> "Red Velvet Cake"
            29 -> "Android 10 / Q"
            28 -> "Pie"
            27 -> "Oreo"
            else -> sdkInt.toString()
        }
    }
}
