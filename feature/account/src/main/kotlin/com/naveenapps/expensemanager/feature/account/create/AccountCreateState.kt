package com.naveenapps.expensemanager.feature.account.create

import androidx.annotation.DrawableRes
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
    val totalAmount: String,
    @DrawableRes val totalAmountBackgroundColor: Int,
    val showDeleteButton: Boolean,
    val showDeleteDialog: Boolean,
)