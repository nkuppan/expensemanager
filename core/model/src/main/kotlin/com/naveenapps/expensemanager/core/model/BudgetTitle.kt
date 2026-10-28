package com.naveenapps.expensemanager.core.model

import java.util.Date

/**
 * What a budget is called on screen: the user's own name, or an automatic one built from its
 * period. Kept as data (not text) so the screens can translate it.
 */
sealed interface BudgetTitle {

    /** The name the user gave the budget. */
    data class Custom(val name: String) : BudgetTitle

    /** A monthly budget for the current month: "This month's budget". */
    data object ThisMonth : BudgetTitle

    /** A yearly budget for the current year: "This year's budget". */
    data object ThisYear : BudgetTitle

    /** A monthly budget for another month: "October 2026 budget". */
    data class Month(val date: Date) : BudgetTitle

    /** A yearly budget for another year ("2025 budget"), or a period key that couldn't be read. */
    data class Period(val label: String) : BudgetTitle
}
