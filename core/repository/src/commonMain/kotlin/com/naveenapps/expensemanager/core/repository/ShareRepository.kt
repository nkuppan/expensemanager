package com.naveenapps.expensemanager.core.repository

interface ShareRepository {

    // File arguments are absolute paths (not java.io.File) so this interface stays platform-neutral.
    fun sendEmail(filePath: String?)

    fun share(filePath: String?)

    fun print(filePath: String?)

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
