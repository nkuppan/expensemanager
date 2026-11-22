package com.naveenapps.expensemanager.core.common.utils

import android.content.Context
import com.naveenapps.expensemanager.core.common.R

/**
 * String resource for a built-in category's stable key (`Category.defaultKey`, stored in the
 * `default_category_key` column), so its name follows the app's language. Null for user-created
 * categories (no key) and unknown keys: those show their stored `name` as typed.
 *
 * This is the only place that maps keys to Android resources; Compose code uses
 * `Category.displayName()` from core:designsystem.
 */
fun defaultCategoryTitleRes(defaultKey: String?): Int? = when (defaultKey) {
    "clothing" -> R.string.category_clothing
    "entertainment" -> R.string.category_entertainment
    "food" -> R.string.category_food
    "health" -> R.string.category_health
    "leisure" -> R.string.category_leisure
    "shopping" -> R.string.category_shopping
    "transportation" -> R.string.category_transportation
    "utilities" -> R.string.category_utilities
    "salary" -> R.string.category_salary
    "gift" -> R.string.category_gift
    "coupons" -> R.string.category_coupons
    else -> null
}

/** A category's display name outside Compose: the translated built-in name, else the stored name. */
fun Context.localizedCategoryName(defaultKey: String?, name: String): String = defaultCategoryTitleRes(defaultKey)?.let(::getString) ?: name
