package com.naveenapps.expensemanager.core.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import java.net.URLEncoder

/**
 * Store links for sharing the app. Each carries UTM parameters in Play's `referrer`, so installs
 * from shares show up under their own source in Play Console's acquisition report (and in the
 * install referrer Firebase reads), split by where in the app the share started.
 */
object AppShareLink {
    /** The release app's package, never the ".debug" one a debug build runs as. */
    private const val PACKAGE = "com.naveenapps.expensemanager"

    const val SOURCE = "app_share"
    const val CAMPAIGN = "referral"

    /** Where the share started, as utm_medium. */
    const val MEDIUM_HOME_BANNER = "home_banner"
    const val MEDIUM_SETTINGS = "settings"

    /**
     * https://play.google.com/store/apps/details?id=…&referrer=utm_source%3Dapp_share%26… .
     * The referrer value is URL-encoded as a whole, as Play requires.
     */
    fun playStore(medium: String): String {
        val referrer = "utm_source=$SOURCE&utm_medium=$medium&utm_campaign=$CAMPAIGN"
        return "https://play.google.com/store/apps/details?id=$PACKAGE&referrer=" +
            URLEncoder.encode(referrer, "UTF-8")
    }
}

/**
 * Opens the system share sheet with [message], which should contain [AppShareLink.playStore].
 * Returns false when no app can receive a share.
 */
fun Context.shareAppMessage(message: String, chooserTitle: String): Boolean {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    return try {
        startActivity(Intent.createChooser(send, chooserTitle))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
