package com.nkuppan.expensemanager.core.ui.utils

import androidx.annotation.DrawableRes
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.AccountType

fun getCurrency(
    currencySymbol: Int,
    amount: Double
) = UiText.StringResource(
    R.string.amount_string,
    UiText.StringResource(currencySymbol),
    amount
)

fun getBalanceCurrency(
    currencySymbol: Int,
    amount: Double
) = UiText.StringResource(
    R.string.balance_amount_string,
    getCurrency(currencySymbol, amount)
)


@DrawableRes
fun AccountType.getPaymentModeIcon(): Int {
    return when (this) {
        AccountType.CREDIT_CARD -> R.drawable.savings
        AccountType.BANK_ACCOUNT -> R.drawable.account_balance
        AccountType.CASH -> R.drawable.credit_card
    }
}

fun AccountType.getPaymentModeIconName(): String {
    return when (this) {
        AccountType.CREDIT_CARD -> "savings"
        AccountType.BANK_ACCOUNT -> "account_balance"
        AccountType.CASH -> "credit_card"
    }
}