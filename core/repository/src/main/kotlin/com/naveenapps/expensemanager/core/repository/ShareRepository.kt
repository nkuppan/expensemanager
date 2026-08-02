package com.naveenapps.expensemanager.core.repository

import java.io.File

interface ShareRepository {

    fun sendEmail(file: File?)

    fun share(file: File?)

    fun print(file: File?)

    fun openRateUs()

    /**
     * Requests Google Play's native in-app review popup (not a Play Store listing deep link —
     * see [openRateUs] for that). Play decides whether to actually display it (quota-limited,
     * independent of app logic) and never reports back whether the user rated or dismissed it,
     * so callers should mark the request as "done" up front and never call this again for the
     * same eligibility window. Safe to call speculatively; failures are swallowed silently.
     */
    suspend fun requestInAppReview()

    fun openPrivacy()

    fun openTerms()

    fun openAboutUs()

    fun openGithub()

    fun openInstagram()

    fun openTwitter()

    fun openAppSettings()
}