package com.naveenapps.expensemanager.core.common.utils


fun Float.toPercentString(): String {
    return String.format("%.2f %%", this)
}