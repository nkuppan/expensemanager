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
    const val TRANSACTION_CREATED = "transaction_created"
    const val TRANSACTION_UPDATED = "transaction_updated"
    const val TRANSACTION_DELETED = "transaction_deleted"

    // Re-engagement
    const val NOTIFICATION_PERMISSION_RESULT = "notification_permission_result"
    const val REMINDER_OPENED = "reminder_opened"
    const val QUICK_ADD_OPENED = "quick_add_opened"
    const val REMINDER_SHOWN = "reminder_shown"
    const val REMINDER_SKIPPED_ALREADY_LOGGED = "reminder_skipped_already_logged"
    const val REMINDER_NO_SPEND = "reminder_no_spend"
    const val WEEKLY_SUMMARY_SHOWN = "weekly_summary_shown"
    const val BUDGET_ALERT_SHOWN = "budget_alert_shown"
    const val NOTIFICATION_PRIMER_SHOWN = "notification_primer_shown"
    const val NOTIFICATION_PRIMER_ACCEPTED = "notification_primer_accepted"
    const val NOTIFICATION_PRIMER_DISMISSED = "notification_primer_dismissed"

    // Feature adoption
    const val ACCOUNT_CREATED = "account_created"
    const val BUDGET_CREATED = "budget_created"
    const val EXPORT_DONE = "export_done"
    const val BACKUP_STARTED = "backup_started"
    const val RESTORE_STARTED = "restore_started"

    // Referral / store
    const val FIRST_SAVE_CELEBRATION_SHOWN = "first_save_celebration_shown"
    const val FIRST_SAVE_ADD_ANOTHER = "first_save_add_another"
    const val FIRST_SAVE_DONE = "first_save_done"

    const val RECAP_CARD_SHOWN = "recap_card_shown"
    const val RECAP_SHARED = "recap_shared"
    const val RECAP_DISMISSED = "recap_dismissed"
    const val SHARE_APP = "share_app"
    const val STREAK_NO_SPEND = "streak_no_spend"
    const val STREAK_MILESTONE = "streak_milestone"

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
    const val ACCOUNT_COUNT = "account_count"
    const val GRANTED = "granted"
    const val FILE_TYPE = "file_type"
    const val METHOD = "method"
    const val SOURCE = "source"
    const val THRESHOLD = "threshold"
    const val DAYS = "days"
    const val REMINDER_ENABLED = "reminder_enabled"
}
