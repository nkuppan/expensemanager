package com.nkuppan.expensemanager.domain.model

import com.nkuppan.expensemanager.R
import java.io.Serializable

data class Currency(
    val type: Int,
    val name: Int,
    val icon: Int,
) : Serializable

fun Currency.getCurrencyIcon(): Int {
    return when (type) {
        R.string.dollar_type -> R.drawable.currency_dollar
        R.string.euro_type -> R.drawable.currency_euro
        R.string.pound_type -> R.drawable.currency_pound
        R.string.rupee_type -> R.drawable.currency_rupee
        R.string.yen_type -> R.drawable.currency_yen
        R.string.yuan_type -> R.drawable.currency_yuan
        R.string.swiss_franc_type -> R.drawable.currency_franc
        R.string.ruble_type -> R.drawable.currency_ruble
        R.string.lira_type -> R.drawable.currency_lira
        else -> R.drawable.currency_dollar
    }
}