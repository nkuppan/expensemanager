package com.naveenapps.expensemanager.feature.account.create

import androidx.annotation.DrawableRes
import com.naveenapps.expensemanager.core.model.AccountDeleteImpact
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue

data class AccountCreateState(
    val name: TextFieldValue<String>,
    val type: TextFieldValue<AccountType>,
    val color: TextFieldValue<String>,
    val icon: TextFieldValue<String>,
    val creditLimit: TextFieldValue<String>,
    val amount: TextFieldValue<String>,
    val currency: Currency,
    /** Null until determined: never a placeholder that could be saved without the user seeing it. */
    val accountCurrency: Currency?,
    val accountCurrencyError: Boolean = false,
    /** Accounts with transactions can only be archived, never deleted. */
    val hasTransactions: Boolean = false,
    val showCurrencySelection: Boolean = false,
    val totalAmount: String,
    @DrawableRes val totalAmountBackgroundColor: Int,
    val showDeleteButton: Boolean,
    val showDeleteDialog: Boolean,
    val customImagePath: String? = null,
    // Loaded when the delete dialog opens; null if it couldn't be determined.
    val deleteImpact: AccountDeleteImpact? = null,
)
