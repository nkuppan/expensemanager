package com.naveenapps.expensemanager.core.repository

interface VersionCheckerRepository {

    fun isAndroidQAndAbove(): Boolean

    fun isAndroidTiramisuAndAbove(): Boolean
}
