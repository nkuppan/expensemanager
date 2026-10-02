package com.naveenapps.expensemanager

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

/**
 * "Quick add" entry points that open the app straight on the new-transaction keypad:
 * the home-screen widget's "+" button, the launcher shortcut, and the daily reminder.
 */
object QuickAdd {

    /** Used by the widget and the launcher shortcut (res/xml/shortcuts.xml). */
    const val ACTION_ADD_TRANSACTION = "com.naveenapps.expensemanager.ADD_TRANSACTION"

    /** Set by NotificationScheduler on the daily reminder's tap intent. */
    private const val ACTION_REMINDER = "add_transaction"

    const val EXTRA_SOURCE = "quick_add_source"

    const val SOURCE_WIDGET = "widget"
    const val SOURCE_SHORTCUT = "shortcut"
    const val SOURCE_REMINDER = "reminder"

    /** Where a launch intent came from, or null when it isn't a quick-add launch. */
    fun sourceOf(intent: Intent?): String? = when (intent?.action) {
        ACTION_REMINDER -> SOURCE_REMINDER
        ACTION_ADD_TRANSACTION -> intent.getStringExtra(EXTRA_SOURCE) ?: SOURCE_SHORTCUT
        else -> null
    }

    private const val SHORTCUT_ID = "add_expense"

    /**
     * Long-press launcher shortcut "Add expense". Published from code rather than a static
     * res/xml/shortcuts.xml because a static intent needs a hard-coded targetPackage, which
     * breaks the ".debug" build. Idempotent (same id), and failures (launcher rate limits,
     * no shortcut support) are ignored: it's a convenience, never worth a crash.
     */
    fun publishShortcut(context: Context) {
        runCatching {
            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
                .setShortLabel(context.getString(R.string.shortcut_add_expense_short))
                .setLongLabel(context.getString(R.string.shortcut_add_expense_long))
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_add_expense))
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(ACTION_ADD_TRANSACTION)
                        .putExtra(EXTRA_SOURCE, SOURCE_SHORTCUT),
                )
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        }
    }

    fun widgetIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
        .setAction(ACTION_ADD_TRANSACTION)
        .putExtra(EXTRA_SOURCE, SOURCE_WIDGET)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
}
