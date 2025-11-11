package com.naveenapps.expensemanager.core.data.utils

import android.content.Context

fun Context.convertFileToString(fileName: String): String? {
    return kotlin.runCatching {
        return this.assets.open(fileName).reader().readText()
    }.getOrNull()
}