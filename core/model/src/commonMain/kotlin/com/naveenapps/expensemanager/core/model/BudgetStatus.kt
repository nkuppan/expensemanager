package com.naveenapps.expensemanager.core.model

/**
 * How much of a budget is spent, by meaning: up to and including 35 % [SAFE], up to 60 % [WATCH],
 * up to 85 % [RISK], above that [OVER]. Models carry this; the UI picks the colour
 * (`emColorOf(status)`: the budget tokens of `EmTheme.colors`).
 */
enum class BudgetStatus {
    SAFE,
    WATCH,
    RISK,
    OVER,
}
