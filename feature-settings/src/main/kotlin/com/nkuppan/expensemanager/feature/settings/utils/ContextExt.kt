package com.nkuppan.expensemanager.feature.settings.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nkuppan.expensemanager.feature.settings.R


fun Context.openWebPage(webpage: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webpage))
    Intent.createChooser(intent, getString(R.string.choose_web_browser))
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.
    }
}

fun Context.openEmailToOption(emailId: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        putExtra(Intent.EXTRA_SUBJECT, "Email subject")
        putExtra(Intent.EXTRA_TEXT, "Email message text")
    }
    Intent.createChooser(intent, getString(R.string.choose_email_app))
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.
    }
}
