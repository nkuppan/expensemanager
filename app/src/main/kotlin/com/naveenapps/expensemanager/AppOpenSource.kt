package com.naveenapps.expensemanager

import android.content.Intent
import com.naveenapps.expensemanager.core.notification.ACTION_OPEN_AUTO_BACKUP
import com.naveenapps.expensemanager.core.notification.EXTRA_OPEN_SOURCE
import com.naveenapps.expensemanager.core.notification.NotificationOpenSource
import com.naveenapps.expensemanager.widget.ACTION_OPEN_FROM_WIDGET

/**
 * How the app was opened, reported as the "source" of app_opened. Notification taps carry
 * [EXTRA_OPEN_SOURCE] (weekly_summary, budget_alert, upcoming_payment, backup_failed).
 */
object AppOpenSource {
    const val LAUNCHER = "launcher"
    const val REMINDER = "reminder"
    const val WIDGET = "widget"
    const val WIDGET_ADD = "widget_add"
    const val SHORTCUT = "shortcut"
    const val SHARE = "share"

    /** Back to an app already in memory with no new intent: Recents, or the launcher icon. */
    const val RESUME = "resume"

    /** The process was killed in the background and Android restored the app (usually Recents). */
    const val RESTORED = "restored"

    const val OTHER = "other"

    fun of(intent: Intent?): String {
        intent?.getStringExtra(EXTRA_OPEN_SOURCE)?.let { return it }
        return when (intent?.action) {
            Intent.ACTION_SEND -> SHARE

            ACTION_OPEN_FROM_WIDGET -> WIDGET

            ACTION_OPEN_AUTO_BACKUP -> NotificationOpenSource.BACKUP_FAILED

            else -> when (QuickAdd.sourceOf(intent)) {
                QuickAdd.SOURCE_REMINDER -> REMINDER
                QuickAdd.SOURCE_WIDGET -> WIDGET_ADD
                QuickAdd.SOURCE_SHORTCUT -> SHORTCUT
                else -> if (intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true) LAUNCHER else OTHER
            }
        }
    }
}
