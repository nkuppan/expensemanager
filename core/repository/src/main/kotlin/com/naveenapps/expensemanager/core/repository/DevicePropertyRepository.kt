package com.naveenapps.expensemanager.core.repository

interface DevicePropertyRepository {

    fun getDeviceName(): String

    fun getDeviceBrandName(): String

    fun getDeviceOsVersion(): String

    fun getDeviceOsVersionNumber(): String
}