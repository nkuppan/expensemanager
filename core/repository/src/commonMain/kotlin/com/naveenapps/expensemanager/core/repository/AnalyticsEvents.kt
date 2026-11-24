package com.naveenapps.expensemanager.core.repository

/**
 * Single source of truth for analytics event and parameter names, so a typo can never split one
 * funnel step into two events in Firebase. Firebase limits: names <= 40 chars, [a-z0-9_] only,
 * string param values <= 100 chars, max 25 params per event.
 */
object AnalyticsEvents {
    // Activation funnel
    const val INTRO_GET_STARTED = "intro_get_started"
    const val ONBOARDING_COMPLETED = "onboarding_completed"

    /** Setup pre-selected the currency for the user's country (`country`, `currency`, `source`). */
    const val ONBOARDING_CURRENCY_DETECTED = "onboarding_currency_detected"

    /** The user picked a different currency during setup (`currency`, `country`). */
    const val ONBOARDING_CURRENCY_CHANGED = "onboarding_currency_changed"

    /** The user opened the account list from setup. */
    const val ONBOARDING_ACCOUNTS_OPENED = "onboarding_accounts_opened"
    const val TRANSACTION_CREATED = "transaction_created"
    const val TRANSACTION_UPDATED = "transaction_updated"
    const val TRANSACTION_DELETED = "transaction_deleted"

    /** Left the Add screen for a new transaction without saving (with stage, fields, seconds). */
    const val TRANSACTION_FORM_ABANDONED = "transaction_form_abandoned"

    /** Save tapped but refused (reason: same_account; a missing amount isn't reported). */
    const val TRANSACTION_SAVE_BLOCKED = "transaction_save_blocked"

    /** Filters sheet applied (type list, account and category counts; never which ones). */
    const val FILTER_APPLIED = "filter_applied"

    /** Period picked in the date sheet (type: this_month, this_year, custom, ...). */
    const val DATE_RANGE_CHANGED = "date_range_changed"

    /** The < / > arrows beside the period (method: back / forward). */
    const val DATE_RANGE_MOVED = "date_range_moved"

    /** A search the user settled on (count: result bucket; type: all_time / period). */
    const val SEARCH_USED = "search_used"

    /** Opened from Analysis (type: category / category_list / transaction). */
    const val ANALYSIS_DRILL_DOWN = "analysis_drill_down"

    /** A frequent-entry chip filled the add form ("transaction_created" then carries source). */
    const val QUICK_PICK_USED = "quick_pick_used"

    /** A UPI / bank message or screenshot was shared in (type: text or image, found_amount). */
    const val RECEIPT_SHARED = "receipt_shared"

    /** "Scan receipt" tapped on the add screen, then a page was scanned (type, found_amount). */
    const val RECEIPT_SCAN_OPENED = "receipt_scan_opened"
    const val RECEIPT_SCANNED = "receipt_scanned"

    /** The scanner's Play services module wasn't on the phone, so a download was requested. */
    const val RECEIPT_SCANNER_DOWNLOAD = "receipt_scanner_download"

    // Re-engagement
    const val NOTIFICATION_PERMISSION_RESULT = "notification_permission_result"
    const val REMINDER_OPENED = "reminder_opened"

    /**
     * Every time the app comes to the front, with "source": launcher, reminder, widget,
     * widget_add, shortcut, share, weekly_summary, budget_alert, upcoming_payment,
     * backup_failed, resume (Recents or the launcher icon while still in memory), restored
     * (reopened after Android closed it in the background) or other.
     */
    const val APP_OPENED = "app_opened"
    const val QUICK_ADD_OPENED = "quick_add_opened"

    /** The home-screen widget's body was tapped to open the app (its "+" logs QUICK_ADD_OPENED). */
    const val WIDGET_OPENED = "widget_opened"
    const val REMINDER_SHOWN = "reminder_shown"
    const val REMINDER_SKIPPED_ALREADY_LOGGED = "reminder_skipped_already_logged"

    /** The reminder's job ran but Android wouldn't show it (reason: permission, blocked, …). */
    const val REMINDER_NOT_SHOWN = "reminder_not_shown"

    /** Found on app start: a reminder was due over 2 hours ago and its job never ran. */
    const val REMINDER_MISSED = "reminder_missed"

    /** Reminder screen: a delivery fix button (reason: notifications_off or battery). */
    const val REMINDER_FIX_OPENED = "reminder_fix_opened"
    const val REMINDER_NO_SPEND = "reminder_no_spend"

    /** A quick pick ("Tea ₹20") was logged straight from the daily reminder's button. */
    const val REMINDER_QUICK_LOGGED = "reminder_quick_logged"

    /** "Undo" on the reminder right after a quick pick was logged. */
    const val REMINDER_QUICK_LOG_UNDONE = "reminder_quick_log_undone"
    const val WEEKLY_SUMMARY_SHOWN = "weekly_summary_shown"
    const val BUDGET_ALERT_SHOWN = "budget_alert_shown"
    const val NOTIFICATION_PRIMER_SHOWN = "notification_primer_shown"
    const val NOTIFICATION_PRIMER_ACCEPTED = "notification_primer_accepted"
    const val NOTIFICATION_PRIMER_DISMISSED = "notification_primer_dismissed"

    /** Home's "Daily reminders are off" banner. `method` on turn-on: dialog / settings / switch. */
    const val REMINDER_BANNER_SHOWN = "reminder_banner_shown"
    const val REMINDER_BANNER_TURN_ON = "reminder_banner_turn_on"
    const val REMINDER_BANNER_DISMISSED = "reminder_banner_dismissed"

    // Feature adoption
    const val ACCOUNT_CREATED = "account_created"
    const val ACCOUNT_EDITED = "account_edited"
    const val ACCOUNT_DELETED = "account_deleted"
    const val CATEGORY_CREATED = "category_created"
    const val CATEGORY_EDITED = "category_edited"
    const val CATEGORY_DELETED = "category_deleted"
    const val BUDGET_CREATED = "budget_created"
    const val BUDGET_EDITED = "budget_edited"
    const val BUDGET_DELETED = "budget_deleted"

    // Debt ledger (never amounts or names). type: lent / borrowed / received / paid.
    const val DEBT_ENTRY_SAVED = "debt_entry_saved"
    const val DEBT_ENTRY_DELETED = "debt_entry_deleted"
    const val DEBT_PERSON_DELETED = "debt_person_deleted"
    const val DEBT_REMINDER_SHARED = "debt_reminder_shared"
    const val DEBT_DUE_REMINDER_SHOWN = "debt_due_reminder_shown"

    /** A setting changed (type: which setting; method: the new value, a choice not free text). */
    const val SETTING_CHANGED = "setting_changed"

    /** Something the user tried failed (type: what, e.g. "export"; reason: error class). */
    const val ACTION_FAILED = "action_failed"
    const val EXPORT_DONE = "export_done"
    const val BACKUP_STARTED = "backup_started"
    const val RESTORE_STARTED = "restore_started"
    const val BACKUP_COMPLETED = "backup_completed"
    const val RESTORE_COMPLETED = "restore_completed"
    const val AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val AUTO_BACKUP_DISABLED = "auto_backup_disabled"
    const val AUTO_BACKUP_COMPLETED = "auto_backup_completed"
    const val AUTO_BACKUP_FAILED = "auto_backup_failed"
    const val AUTO_BACKUP_FIX_OPENED = "auto_backup_fix_opened"
    const val RECURRING_CREATED = "recurring_created"
    const val RECURRING_GENERATED = "recurring_generated"
    const val RECURRING_STOPPED = "recurring_stopped"
    const val RECURRING_EDITED = "recurring_edited"

    /** Evening heads-up before weekly / monthly / yearly recurring entries; `count` = how many tomorrow. */
    const val UPCOMING_ALERT_SHOWN = "upcoming_alert_shown"
    const val UPCOMING_ALERTS_ON = "upcoming_alerts_on"
    const val UPCOMING_ALERTS_OFF = "upcoming_alerts_off"

    // Referral / store

    /** `source` = where it started: "home_banner" or "settings" (same as the link's utm_medium). */
    const val SHARE_APP = "share_app"
    const val SHARE_BANNER_SHOWN = "share_banner_shown"
    const val SHARE_BANNER_DISMISSED = "share_banner_dismissed"

    /** Saved a new Home section order (Customise dashboard). */
    const val DASHBOARD_CUSTOMISED = "dashboard_customised"

    /** Home's bell opened the "What's new" sheet; `source` = whether it had an unread dot. */
    const val WHATS_NEW_OPENED = "whats_new_opened"

    const val REVIEW_REQUESTED = "review_requested"
    const val RATE_US_CLICKED = "rate_us_clicked"
}

object AnalyticsParams {
    const val TYPE = "type"
    const val IS_FIRST = "is_first"
    const val TRANSACTION_COUNT = "transaction_count"
    const val HAS_NOTES = "has_notes"
    const val HAS_ATTACHMENT = "has_attachment"
    const val CURRENCY = "currency"
    const val COUNTRY = "country"
    const val ACCOUNT_COUNT = "account_count"
    const val CATEGORY_COUNT = "category_count"

    /** Debt entry: "true" when it moved money in one of the user's accounts. */
    const val LINKED_ACCOUNT = "linked_account"
    const val GRANTED = "granted"

    /** Reminders banner: "true" when it offered a time based on when the user logs. */
    const val SUGGESTED_TIME = "suggested_time"
    const val FILE_TYPE = "file_type"
    const val METHOD = "method"
    const val SOURCE = "source"
    const val DAYS = "days"
    const val REMINDER_ENABLED = "reminder_enabled"
    const val COUNT = "count"
    const val REASON = "reason"
    const val FREQUENCY = "frequency"

    /** Minutes between the reminder time the user picked and when it actually ran. */
    const val LATE_MINUTES = "late_minutes"

    /** How far a form got: untouched / details (whether an amount was typed is never sent). */
    const val STAGE = "stage"

    /** Comma-separated names of the fields the user changed, e.g. "category,notes" (never amount). */
    const val FIELDS = "fields"

    /** Seconds spent, e.g. on the Add screen before saving or leaving. */
    const val SECONDS = "seconds"
}
