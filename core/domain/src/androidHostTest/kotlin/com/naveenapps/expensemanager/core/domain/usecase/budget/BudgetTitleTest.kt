package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.foundation.toLocalDateIn
import com.naveenapps.expensemanager.core.foundation.toMonthAndYearKey
import com.naveenapps.expensemanager.core.foundation.toYearKey
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetTitle
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Test

class BudgetTitleTest {

    private val now: Instant = LocalDateTime(2026, 10, 15, 12, 0).toInstant(TimeZone.currentSystemDefault())
    private val thisMonth = now.toLocalDateIn().toMonthAndYearKey()
    private val thisYear = now.toLocalDateIn().toYearKey()

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
            .isEqualTo(BudgetTitle.Month(LocalDate(2026, 3, 1)))
        assertThat(budgetTitle(null, "2025", BudgetPeriod.YEARLY, now)).isEqualTo(BudgetTitle.Period("2025"))
    }

    @Test
    fun `an unreadable month key is shown as it is`() {
        assertThat(budgetTitle(null, "garbage", BudgetPeriod.MONTHLY, now)).isEqualTo(BudgetTitle.Period("garbage"))
    }
}
