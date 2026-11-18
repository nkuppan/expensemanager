package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.common.R
import org.junit.Test

class BudgetProgressTest {

    @Test
    fun percentIsTheShareOfTheBudgetSpent() {
        assertThat(budgetSpentPercent(spent = 250.0, budgetAmount = 1000.0)).isWithin(0.001f).of(25f)
        assertThat(budgetSpentPercent(spent = 1500.0, budgetAmount = 1000.0)).isWithin(0.001f).of(150f)
        assertThat(budgetSpentPercent(spent = 0.0, budgetAmount = 1000.0)).isEqualTo(0f)
    }

    @Test
    fun aZeroBudgetDoesNotDivideByZero() {
        assertThat(budgetSpentPercent(spent = 0.0, budgetAmount = 0.0)).isEqualTo(0f)
        assertThat(budgetSpentPercent(spent = 10.0, budgetAmount = 0.0)).isEqualTo(100f)
    }

    @Test
    fun colourFollowsTheShareSpent() {
        assertThat(budgetProgressColor(0f)).isEqualTo(R.color.green_500)
        assertThat(budgetProgressColor(35f)).isEqualTo(R.color.green_500)
        assertThat(budgetProgressColor(50f)).isEqualTo(R.color.light_green_500)
        assertThat(budgetProgressColor(60f)).isEqualTo(R.color.light_green_500)
        assertThat(budgetProgressColor(80f)).isEqualTo(R.color.orange_500)
        assertThat(budgetProgressColor(85f)).isEqualTo(R.color.orange_500)
        assertThat(budgetProgressColor(85.1f)).isEqualTo(R.color.red_500)
        assertThat(budgetProgressColor(140f)).isEqualTo(R.color.red_500)
    }

    @Test
    fun fractionalPercentagesBetweenTheOldRangesAreNotShownAsOverBudget() {
        // The old integer ranges (0..35, 36..60, 61..85) made these fall through to red.
        assertThat(budgetProgressColor(35.5f)).isEqualTo(R.color.light_green_500)
        assertThat(budgetProgressColor(60.4f)).isEqualTo(R.color.orange_500)
    }
}
