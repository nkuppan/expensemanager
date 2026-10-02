package com.naveenapps.expensemanager.core.data.repository

import android.os.Build
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository

class VersionCheckerRepositoryImpl : VersionCheckerRepository {

    override fun isAndroidQAndAbove(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    override fun isAndroidTiramisuAndAbove(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}
