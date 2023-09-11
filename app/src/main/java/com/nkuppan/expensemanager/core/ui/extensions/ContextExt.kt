package com.nkuppan.expensemanager.core.ui.extensions

import android.annotation.SuppressLint
import android.content.Context
import com.nkuppan.expensemanager.R


@SuppressLint("DiscouragedApi")
fun Context.getDrawable(iconName: String): Int {
    return runCatching {
        val resources = this.resources.getIdentifier(
            iconName, "drawable", this.packageName
        )

        if (resources > 0) resources else null
    }.getOrNull() ?: R.drawable.ic_calendar
}