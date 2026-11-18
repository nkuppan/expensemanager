package com.naveenapps.expensemanager.core.repository

interface DevicePropertyRepository {

    fun getDeviceName(): String

    fun getDeviceBrandName(): String

    fun getDeviceOsVersion(): String

    fun getDeviceOsVersionNumber(): String

    fun getAppVersion(): String

    /**
     * Where the user likely is, as ISO country codes ("IN"), most reliable first: the SIM's
     * country, then the mobile network's, then the phone's region setting. No permission needed.
     */
    fun getRegionCountryCodes(): List<RegionGuess>
}

/** A country guess and where it came from ("sim", "network" or "locale"), for analytics. */
data class RegionGuess(val countryCode: String, val source: String)
