package com.naveenapps.expensemanager.feature.transaction.search

/**
 * Shared, case-insensitive "contains" match used by the transaction and recurring lists so both
 * search boxes behave the same. A blank query matches everything.
 */
internal fun matchesSearch(query: String, fields: List<String?>): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    return fields.any { it?.contains(needle, ignoreCase = true) == true }
}
