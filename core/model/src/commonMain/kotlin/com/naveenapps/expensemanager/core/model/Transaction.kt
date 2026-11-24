package com.naveenapps.expensemanager.core.model

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A logged expense, income or transfer. Immutable: the data layer attaches [category], the
 * accounts and [attachments] with copy(), so one instance can safely feed every screen from a
 * shared stream (a screen changing it in place would have changed it for all of them).
 */
data class Transaction(
    val id: String,
    val notes: String,
    val categoryId: String,
    val fromAccountId: String,
    val toAccountId: String?,
    val amount: Amount,
    val imagePath: String,
    val type: TransactionType,
    val createdOn: Instant,
    val updatedOn: Instant,
    val category: Category = Category(
        "",
        "",
        CategoryType.INCOME,
        StoredIcon("", ""),
        Clock.System.now(),
        Clock.System.now(),
    ),
    val fromAccount: Account = Account(
        "",
        "",
        AccountType.REGULAR,
        StoredIcon("", ""),
        Clock.System.now(),
        Clock.System.now(),
    ),
    val toAccount: Account? = null,
    val attachments: List<String> = emptyList(),
)

/**
 * Id prefix of the transactions a recurring rule logged, followed by "<ruleId>-<occurrence>".
 * See ProcessRecurringTransactionsUseCase, which creates them.
 */
const val RECURRING_OCCURRENCE_ID_PREFIX = "recurring-"

/** True when a recurring rule added this transaction automatically. */
val Transaction.isRecurringOccurrence: Boolean get() = id.startsWith(RECURRING_OCCURRENCE_ID_PREFIX)
