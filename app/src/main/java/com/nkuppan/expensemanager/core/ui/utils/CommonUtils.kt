package com.nkuppan.expensemanager.core.ui.utils

import androidx.annotation.DrawableRes
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.PaymentMode

fun getCurrency(
    currencySymbol: Int,
    amount: Double
) = UiText.StringResource(
    R.string.amount_string,
    UiText.StringResource(currencySymbol),
    amount
)


@DrawableRes
fun PaymentMode.getPaymentModeIcon(): Int {
    return when (this) {
        PaymentMode.CARD -> R.drawable.credit_card
        PaymentMode.WALLET -> R.drawable.wallet
        PaymentMode.UPI -> R.drawable.qr_code
        PaymentMode.CHEQUE -> R.drawable.payments
        PaymentMode.INTERNET_BANKING -> R.drawable.netbanking
        PaymentMode.BANK_ACCOUNT -> R.drawable.account_balance
        PaymentMode.NONE -> R.drawable.wallet
    }
}