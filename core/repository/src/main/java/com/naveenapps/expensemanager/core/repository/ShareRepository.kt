package com.naveenapps.expensemanager.core.repository

import java.io.File

interface ShareRepository {

    fun sendEmail(file: File?)

    fun share(file: File?)

    fun openPrinter(file: File?)

    fun openRateUs()

    fun openPrivacy()
}