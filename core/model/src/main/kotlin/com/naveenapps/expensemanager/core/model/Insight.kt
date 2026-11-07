package com.naveenapps.expensemanager.core.model

/** What kind of insight a pushed notification was; stored by [name], so never rename one. */
enum class InsightKind {
    WEEKLY_SUMMARY,
    BUDGET_ALERT,
    ;

    companion object {
        fun fromStored(name: String?): InsightKind = entries.find { it.name == name } ?: WEEKLY_SUMMARY
    }
}

/**
 * An insight the app pushed (weekly summary, budget alert), kept for the notification centre so
 * it can be read again later, even if notifications were off when it was posted.
 */
data class Insight(
    val kind: InsightKind,
    val title: String,
    val message: String,
    /** Epoch millis. */
    val postedAt: Long,
)
