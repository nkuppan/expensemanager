package com.naveenapps.expensemanager.core.designsystem.ui.extensions

import android.annotation.SuppressLint
import android.content.Context
import com.naveenapps.expensemanager.core.designsystem.R
import java.util.concurrent.ConcurrentHashMap

/**
 * Icon names are stored as strings, so every list row resolves them with `getIdentifier`, a
 * slow reflective lookup that used to run for each icon on every recomposition while
 * scrolling. Resource ids never change at runtime, so each name is resolved once.
 */
private val drawableIdCache = ConcurrentHashMap<String, Int>()

@SuppressLint("DiscouragedApi")
fun Context.getDrawable(iconName: String): Int = drawableIdCache.getOrPut(iconName) {
    runCatching {
        val resources = this.resources.getIdentifier(
            iconName,
            "drawable",
            this.packageName,
        )

        if (resources > 0) resources else null
    }.getOrNull() ?: R.drawable.account_balance
}
