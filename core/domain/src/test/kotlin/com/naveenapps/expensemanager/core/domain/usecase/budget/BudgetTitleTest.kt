package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetTitle
import java.util.Calendar
import java.util.Date
import org.junit.Test

class BudgetTitleTest {

    private val now: Date = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 15, 12, 0, 0) }.time
    private val thisMonth = now.toMonthAndYearKey()
    private val thisYear = now.toYear()

    @Test
    fun `a given name is shown as is, trimmed`() {
        assertThat(budgetTitle("  Groceries ", thisMonth, BudgetPeriod.MONTHLY, now))
            .isEqualTo(BudgetTitle.Custom("Groceries"))
    }

    @Test
    fun `a blank name falls back to the automatic one`() {
        assertThat(budgetTitle("   ", thisMonth, BudgetPeriod.MONTHLY, now)).isEqualTo(BudgetTitle.ThisMonth)
        assertThat(budgetTitle(null, thisYear, BudgetPeriod.YEARLY, now)).isEqualTo(BudgetTitle.ThisYear)
    }

    @Test
    fun `other months and years are named by their period`() {
        val march = "March 2026"

        assertThat(budgetTitle(null, march, BudgetPeriod.MONTHLY, now))
            .isEqualTo(BudgetTitle.Month(march.fromMonthAndYearKey()!!))
        assertThat(budgetTitle(null, "2025", BudgetPeriod.YEARLY, now)).isEqualTo(BudgetTitle.Period("2025"))
    }

    @Test
    fun `an unreadable month key is shown as it is`() {
        assertThat(budgetTitle(null, "garbage", BudgetPeriod.MONTHLY, now)).isEqualTo(BudgetTitle.Period("garbage"))
    }
}
