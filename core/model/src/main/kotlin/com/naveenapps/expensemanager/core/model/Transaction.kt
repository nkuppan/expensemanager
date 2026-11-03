package com.naveenapps.expensemanager.core.model

import java.util.Date

data class Transaction(
    val id: String,
    val notes: String,
    val categoryId: String,
    val fromAccountId: String,
    val toAccountId: String?,
    val amount: Amount,
    val imagePath: String,
    val type: TransactionType,
    val createdOn: Date,
    val updatedOn: Date,
    var category: Category = Category(
        "",
        "",
        CategoryType.INCOME,
        StoredIcon("", ""),
        Date(),
        Date(),
    ),
    var fromAccount: Account = Account(
        "",
        "",
        AccountType.REGULAR,
        StoredIcon("", ""),
        Date(),
        Date(),
    ),
    var toAccount: Account? = null,
    var attachments: List<String> = emptyList(),
)

/**
 * Id prefix of the transactions a recurring rule logged, followed by "<ruleId>-<occurrence>".
 * See ProcessRecurringTransactionsUseCase, which creates them.
 */
const val RECURRING_OCCURRENCE_ID_PREFIX = "recurring-"

/** True when a recurring rule added this transaction automatically. */
val Transaction.isRecurringOccurrence: Boolean get() = id.startsWith(RECURRING_OCCURRENCE_ID_PREFIX)
