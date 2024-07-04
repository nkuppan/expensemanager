package com.naveenapps.expensemanager.core.repository

import java.io.File

interface ShareRepository {

    fun sendEmail(file: File?)

    fun share(file: File?)

    fun print(file: File?)

    fun openRateUs()

    fun openPrivacy()

    fun openTerms()

    fun openAboutUs()

    fun openGithub()

    fun openInstagram()

    fun openTwitter()
}