package com.naveenapps.expensemanager.core.model

/**
 * How a [Budget]'s [Budget.selectedMonth] key should be interpreted and how its spending should
 * be aggregated: against a single calendar month, or across an entire calendar year.
 *
 * MONTHLY must stay the first entry (ordinal 0) — it's persisted as a raw ordinal Int on
 * `BudgetEntity`, and existing rows are backfilled to `0` by migration `MIGRATION_5_6`, so every
 * budget created before this field existed must continue to resolve to MONTHLY.
 */
enum class BudgetPeriod {
    MONTHLY,
    YEARLY,
}
