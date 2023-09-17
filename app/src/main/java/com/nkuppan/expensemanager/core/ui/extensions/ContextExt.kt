package com.nkuppan.expensemanager.core.ui.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import com.nkuppan.expensemanager.R


@SuppressLint("DiscouragedApi")
fun Context.getDrawable(iconName: String): Int {
    return runCatching {
        val resources = this.resources.getIdentifier(
            iconName, "drawable", this.packageName
        )

        if (resources > 0) resources else null
    }.getOrNull() ?: R.drawable.ic_calendar
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

fun launchReviewWorkflow(context: Context) {
    val manager = ReviewManagerFactory.create(context)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // We got the ReviewInfo object
            val reviewInfo = task.result
            reviewInfo?.let {
                val flow = manager.launchReviewFlow(context as Activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            }
        }
    }
}