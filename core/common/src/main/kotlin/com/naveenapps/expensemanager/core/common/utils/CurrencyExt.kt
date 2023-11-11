package com.naveenapps.expensemanager.core.common.utils

import com.naveenapps.expensemanager.core.common.R


fun Int.getCurrencyIcon(): Int {
    return when (this) {
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