package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import com.naveenapps.expensemanager.core.repository.DevicePropertyRepository
import com.naveenapps.expensemanager.core.repository.RegionGuess
import java.util.Locale

class DevicePropertyRepositoryImpl(
    private val context: Context,
) : DevicePropertyRepository {

    override fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        val model = Build.MODEL.lowercase(Locale.getDefault())
        return if (model.startsWith(manufacturer)) {
            Build.MODEL
        } else {
            "${Build.MANUFACTURER} ${Build.MODEL}"
        }
    }

    override fun getDeviceBrandName(): String = Build.BRAND

    override fun getDeviceOsVersion(): String = getOsVersionName()

    override fun getDeviceOsVersionNumber(): String = Build.VERSION.SDK_INT.toString()

    override fun getAppVersion(): String = try {
        val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ""
    }

    override fun getRegionCountryCodes(): List<RegionGuess> {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return listOfNotNull(
            runCatching { telephony?.simCountryIso }.getOrNull()?.let { RegionGuess(it, "sim") },
            runCatching { telephony?.networkCountryIso }.getOrNull()?.let { RegionGuess(it, "network") },
            RegionGuess(Locale.getDefault().country, "locale"),
        )
            .map { it.copy(countryCode = it.countryCode.uppercase(Locale.ROOT)) }
            .filter { it.countryCode.length == 2 }
            .distinctBy { it.countryCode }
    }

    private fun getOsVersionName(): String = when (val sdkInt = Build.VERSION.SDK_INT) {
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
