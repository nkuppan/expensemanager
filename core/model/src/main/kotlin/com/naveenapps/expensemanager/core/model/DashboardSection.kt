package com.naveenapps.expensemanager.core.model

/**
 * The Home sections the user can reorder (Customise dashboard). The filter bar, alerts and the
 * "Get started" card stay on top, and the share banner and Customise button stay at the bottom.
 * Stored by [name], so never rename an entry; the declaration order is the default order.
 */
enum class DashboardSection {
    SUMMARY,
    ACCOUNTS,
    CATEGORIES,
    BUDGETS,
    TRANSACTIONS,
    UPCOMING,
    ;

    companion object {
        val DEFAULT_ORDER: List<DashboardSection> = entries

        /**
         * The saved order, tolerant of change: unknown names (a section removed in a later
         * version) are dropped, duplicates ignored, and sections added since the user last saved
         * are appended in their default place at the end, so a new section never goes missing.
         */
        fun fromStored(names: List<String>?): List<DashboardSection> {
            if (names.isNullOrEmpty()) return DEFAULT_ORDER
            val saved = names.mapNotNull { name -> entries.find { it.name == name } }.distinct()
            return saved + (DEFAULT_ORDER - saved.toSet())
        }
    }
}
