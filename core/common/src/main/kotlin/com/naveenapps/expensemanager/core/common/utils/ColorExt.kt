package com.naveenapps.expensemanager.core.common.utils

import com.naveenapps.expensemanager.core.common.R

fun Double.getAmountTextColor() = if (this < 0) {
    R.color.red_500
} else {
    R.color.green_500
}
