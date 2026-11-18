package com.naveenapps.expensemanager.feature.transaction.create

/**
 * What the user did on the Add screen for a new transaction, for analytics: which fields they
 * changed, how many saves were refused and how long they stayed. Leaving without saving is
 * reported with how far they got, so the drop after onboarding ("opened Add, never saved") can
 * be broken down.
 *
 * Nothing about money is recorded: not the amount, and not even whether one was typed. The
 * amount field is deliberately not tracked; only the other fields are.
 */
internal class AddFormJourney(private val startedAt: Long) {

    private val touched = linkedSetOf<String>()

    /** Save taps refused for a reason unrelated to the amount (same account on a transfer). */
    var blockedSaves: Int = 0
        private set

    fun touch(field: String) {
        touched += field
    }

    fun onSaveBlocked() {
        blockedSaves++
    }

    /** Fields changed, in the order first touched, e.g. "category,notes"; "none" if nothing. */
    val fields: String get() = touched.joinToString(",").ifEmpty { NONE }

    fun seconds(now: Long): Long = ((now - startedAt) / MILLIS_PER_SECOND).coerceAtLeast(0)

    /** "untouched" when no field (other than the amount, which isn't tracked) was changed. */
    val stage: String get() = if (touched.isEmpty()) STAGE_UNTOUCHED else STAGE_DETAILS

    companion object {
        const val FIELD_TYPE = "type"
        const val FIELD_CATEGORY = "category"
        const val FIELD_ACCOUNT = "account"
        const val FIELD_DATE = "date"
        const val FIELD_NOTES = "notes"
        const val FIELD_ATTACHMENT = "attachment"
        const val FIELD_REPEAT = "repeat"
        const val FIELD_QUICK_PICK = "quick_pick"
        const val FIELD_RECEIPT = "receipt"

        const val STAGE_UNTOUCHED = "untouched"
        const val STAGE_DETAILS = "details"

        private const val NONE = "none"
        private const val MILLIS_PER_SECOND = 1_000L
    }
}
