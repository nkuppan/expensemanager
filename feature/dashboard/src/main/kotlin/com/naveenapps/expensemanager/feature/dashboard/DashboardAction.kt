package com.naveenapps.expensemanager.feature.dashboard

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.TransactionUiItem

sealed class DashboardAction {

    data object OpenSettings : DashboardAction()

    data class OpenTransactionEdit(val transaction: TransactionUiItem?) : DashboardAction()

    data object OpenTransactionList : DashboardAction()

    data object OpenBudgetList : DashboardAction()

    /** [period] preselects Monthly or Annual on the create screen. */
    data class OpenBudgetCreate(val period: BudgetPeriod? = null) : DashboardAction()

    data class OpenBudgetDetails(val id: String) : DashboardAction()

    data class EditBudget(val id: String) : DashboardAction()

    data class OpenAccountEdit(val account: AccountUiModel) : DashboardAction()

    data object OpenAccountList : DashboardAction()

    data object DismissGettingStarted : DashboardAction()

    /** "Fix" on the automatic-backup-failed banner. */
    data object FixAutoBackup : DashboardAction()

    /** "Later" on the banner: hides it for a few days, or until the next fix and failure. */
    data object SnoozeAutoBackupWarning : DashboardAction()

    /** "View all" on the Upcoming card. */
    data object OpenRecurringList : DashboardAction()

    data class OpenRecurringEdit(val id: String) : DashboardAction()

    /** Upcoming card's empty state: the add screen with Repeat set to Monthly. */
    data object PlanRecurring : DashboardAction()

    // "Daily reminders are off" banner.
    data object ReminderBannerShown : DashboardAction()

    /**
     * "Turn on": [method] is what the screen did. "dialog" (system permission dialog shown) or
     * "settings" (the app's notification settings opened, once Android stops showing the dialog).
     */
    data class ReminderBannerTurnOn(val method: String) : DashboardAction()

    data class ReminderPermissionResult(val granted: Boolean) : DashboardAction()

    /** Notifications became allowed (e.g. back from Settings): make sure the reminder is on. */
    data object NotificationsAllowed : DashboardAction()

    /** "Not now": hidden for [REMINDER_BANNER_SNOOZE_DAYS] days. */
    data object DismissReminderBanner : DashboardAction()

    /** The bell in Home's top bar: opens the notification centre. */
    data object OpenNotifications : DashboardAction()

    /** "Customise dashboard" at the bottom of Home: reorder the sections. */
    data object OpenCustomiseDashboard : DashboardAction()

    data object ShareBannerShown : DashboardAction()

    /** "Share the app" on the banner; the screen has already opened the share sheet. */
    data object ShareAppFromBanner : DashboardAction()

    /** Close on the banner: hidden for [SHARE_BANNER_SNOOZE_DAYS] days. */
    data object DismissShareBanner : DashboardAction()
}

/** How long "close" hides the share banner. */
internal const val SHARE_BANNER_SNOOZE_DAYS = 30L

/** How long "Not now" hides the reminders banner, and after how many dismissals it stops. */
internal const val REMINDER_BANNER_SNOOZE_DAYS = 14L
internal const val REMINDER_BANNER_MAX_DISMISSALS = 3
