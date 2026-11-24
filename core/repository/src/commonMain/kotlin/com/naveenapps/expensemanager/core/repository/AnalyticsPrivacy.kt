package com.naveenapps.expensemanager.core.repository

/**
 * Money is the user's own business: analytics never carries amounts, totals, balances, prices
 * or budget values, not even whether an amount was entered. Events are reviewed for this, and
 * this check is the safety net: any parameter or user property whose name looks like money is
 * dropped before it reaches Analytics, so a future event can't send it by mistake.
 */
object AnalyticsPrivacy {

    private val MONEY_WORDS = listOf(
        "amount", "total", "balance", "spent", "spend", "price", "cost", "salary",
        "income", "expense", "money", "threshold", "percent", "limit", "value",
    )

    /** True when [name] (a parameter or user property name) may hold money data. */
    fun isBlocked(name: String): Boolean {
        val lower = name.lowercase()
        return MONEY_WORDS.any { it in lower }
    }

    /** [params] without the ones [isBlocked] rejects. */
    fun filter(params: Map<String, String>): Map<String, String> = params.filterKeys { !isBlocked(it) }
}
