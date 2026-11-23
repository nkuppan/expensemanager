package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable

@Stable
data class TransactionGroup(
    val date: String,
    val amountTone: AmountTone,
    val totalAmount: Amount,
    val transactions: List<TransactionUiItem>,
    val isLastItem: Boolean = false,
)

@Stable
data class TransactionUiItem(
    val id: String,
    val amount: Amount = Amount(0.0),
    val notes: String?,
    val categoryName: String,
    val transactionType: TransactionType,
    val categoryIcon: StoredIcon,
    val date: String,
    val fromAccountName: String,
    val fromAccountIcon: StoredIcon,
    val toAccountName: String? = null,
    val toAccountIcon: StoredIcon? = null,
    /** [Category.defaultKey] of the transaction's category; see `categoryDisplayName()`. */
    val categoryDefaultKey: String? = null,
    /** Added automatically by a recurring rule; lists mark it with a small repeat badge. */
    val isRecurring: Boolean = false,
    /**
     * Set for a debt entry shown among transactions ("Lent to Ravi"): [categoryName] then holds
     * the person's name. It moved money in an account but isn't spending or income.
     */
    val debtKind: DebtEntryKind? = null,
    /** Closing / running balance of the account after this transaction occurred. */
    val closingBalance: Amount? = null,
)
