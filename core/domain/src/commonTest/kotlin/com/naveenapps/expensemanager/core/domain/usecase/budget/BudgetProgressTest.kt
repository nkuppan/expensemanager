package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.model.BudgetStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class BudgetProgressTest {

    @Test
    fun percentIsTheShareOfTheBudgetSpent() {
        assertEquals(25f, budgetSpentPercent(spent = 250.0, budgetAmount = 1000.0), 0.001f)
        assertEquals(150f, budgetSpentPercent(spent = 1500.0, budgetAmount = 1000.0), 0.001f)
        assertEquals(0f, budgetSpentPercent(spent = 0.0, budgetAmount = 1000.0))
    }

    @Test
    fun aZeroBudgetDoesNotDivideByZero() {
        assertEquals(0f, budgetSpentPercent(spent = 0.0, budgetAmount = 0.0))
        assertEquals(100f, budgetSpentPercent(spent = 10.0, budgetAmount = 0.0))
    }

    @Test
    fun statusFollowsTheShareSpent() {
        assertEquals(BudgetStatus.SAFE, budgetStatus(0f))
        assertEquals(BudgetStatus.SAFE, budgetStatus(35f))
        assertEquals(BudgetStatus.WATCH, budgetStatus(50f))
        assertEquals(BudgetStatus.WATCH, budgetStatus(60f))
        assertEquals(BudgetStatus.RISK, budgetStatus(80f))
        assertEquals(BudgetStatus.RISK, budgetStatus(85f))
        assertEquals(BudgetStatus.OVER, budgetStatus(85.1f))
        assertEquals(BudgetStatus.OVER, budgetStatus(140f))
    }

    @Test
    fun fractionalPercentagesBetweenTheOldRangesAreNotShownAsOverBudget() {
        // The old integer ranges (0..35, 36..60, 61..85) made these fall through to red.
        assertEquals(BudgetStatus.WATCH, budgetStatus(35.5f))
        assertEquals(BudgetStatus.RISK, budgetStatus(60.4f))
    }
}
