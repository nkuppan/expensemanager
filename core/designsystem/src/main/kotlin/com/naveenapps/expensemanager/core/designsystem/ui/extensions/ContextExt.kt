package com.naveenapps.expensemanager.core.designsystem.ui.extensions

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.naveenapps.expensemanager.core.designsystem.R

@SuppressLint("DiscouragedApi")
fun Context.getDrawable(iconName: String): Int {
    return runCatching {
        val resources = this.resources.getIdentifier(
                iconName,
                "drawable",
                this.packageName,
        )

        if (resources > 0) resources else null
    }.getOrNull() ?: R.drawable.account_balance
}

fun openWebPage(context: Context, webpage: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webpage))
    Intent.createChooser(intent, context.getString(R.string.choose_web_browser))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.

    }
}

fun openEmailToOption(context: Context, emailId: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$emailId"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "email_subject")
        intent.putExtra(Intent.EXTRA_TEXT, "email_body")
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.

    }
}

fun Context.shareThisFile(fileUri: Uri) {
    try {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, null))
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.
    }
}

fun Context.getAppVersionName(): String {
    return try {
        val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        pInfo.versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ""
    }
}


fun Context.convertFileToString(fileName: String): String? {
    return kotlin.runCatching {
        return this.assets.open(fileName).reader().readText()
    }.getOrNull()
}