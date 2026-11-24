package com.naveenapps.expensemanager.core.repository

/**
 * Asks for budgets to be re-checked for the 80% / 100% alerts, e.g. right after a transaction
 * is saved. Implemented in core:notification (it schedules a background check), so feature
 * modules can trigger alerts without depending on the notification module.
 */
interface BudgetAlertTrigger {
    fun checkBudgetsSoon()
}
