package com.naveenapps.expensemanager.feature.debt

import androidx.annotation.StringRes
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/** Short label for an entry kind: "Lent", "Borrowed", "Got back", "Paid back". */
@StringRes
fun DebtEntryKind.labelRes(): Int = when (this) {
    DebtEntryKind.LENT -> R.string.debt_kind_lent
    DebtEntryKind.BORROWED -> R.string.debt_kind_borrowed
    DebtEntryKind.RECEIVED -> R.string.debt_kind_received
    DebtEntryKind.PAID -> R.string.debt_kind_paid
}

/** Where the money went or came from, as the account field's label. */
@StringRes
fun DebtEntryKind.accountLabelRes(): Int = when (this) {
    DebtEntryKind.LENT, DebtEntryKind.PAID -> R.string.debt_account_from
    DebtEntryKind.BORROWED, DebtEntryKind.RECEIVED -> R.string.debt_account_into
}

/** Whether a person's balance means they owe the user, the user owes them, or it's settled. */
enum class DebtStatus { OWES_YOU, YOU_OWE, SETTLED }

fun debtStatusOf(balanceMilli: Long): DebtStatus = when {
    balanceMilli > 0 -> DebtStatus.OWES_YOU
    balanceMilli < 0 -> DebtStatus.YOU_OWE
    else -> DebtStatus.SETTLED
}

/** True when [dueDate] is before the start of today. */
fun isOverdue(
    dueDate: Instant?,
    now: Instant = Clock.System.now(),
    zone: TimeZone = TimeZone.currentSystemDefault(),
): Boolean {
    dueDate ?: return false
    val startOfToday = now.toLocalDateTime(zone).date.atStartOfDayIn(zone)
    return dueDate < startOfToday
}
