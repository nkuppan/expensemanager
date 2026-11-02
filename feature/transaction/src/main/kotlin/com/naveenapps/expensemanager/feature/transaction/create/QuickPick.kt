package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType

/** A frequent entry, resolved against the current categories and accounts, ready to fill the form. */
@Stable
data class QuickPick(
    val type: TransactionType,
    val category: Category,
    val account: AccountUiModel,
    val amount: Double,
    val amountText: String,
    val notes: String,
) {
    val key: String get() = "${type.name}|${category.id}|${account.id}|$amount|$notes"
}
