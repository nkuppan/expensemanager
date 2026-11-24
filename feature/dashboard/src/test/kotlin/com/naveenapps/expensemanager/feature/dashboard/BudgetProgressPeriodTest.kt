package com.naveenapps.expensemanager.feature.dashboard

import com.google.common.truth.Truth.assertWithMessage
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetStatus
import com.naveenapps.expensemanager.core.model.BudgetTitle
import com.naveenapps.expensemanager.feature.budget.components.toBudgetProgress
import java.time.Year
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlinx.datetime.LocalDate
import org.junit.Test

/**
 * The budget period maths (start of the period, days left for "safe to spend per day") on
 * kotlinx-datetime against the java.time version it replaced (KMP groundwork C5), for every
 * month from 2018 to 2032 and "today" before, inside and after each period.
 */
class BudgetProgressPeriodTest {

    // "Safe per day" is left / days left, so with the same amount it differs only if the day count does.
    private val left = 27_720.0
    private val format: (Double) -> String = { it.toString() }

    private fun budget(key: String, period: BudgetPeriod) = BudgetUiModel(
        id = key,
        title = BudgetTitle.ThisMonth,
        selectedMonth = key,
        periodType = period,
        status = BudgetStatus.SAFE,
        amount = Amount(left, "$left"),
        transactionAmount = Amount(0.0, "0"),
        percent = 0f,
    )

    /** java.time version: (first day, days left counting today) as of [today]. */
    private fun legacy(
        start: java.time.LocalDate,
        end: java.time.LocalDate,
        today: java.time.LocalDate,
    ): Pair<java.time.LocalDate, Long> {
        val from = maxOf(today, start)
        val daysLeft = if (from.isAfter(end)) 0L else ChronoUnit.DAYS.between(from, end) + 1
        return start to daysLeft
    }

    private fun todaysAround(start: java.time.LocalDate, end: java.time.LocalDate) = listOf(
        start.minusDays(40), start.minusDays(1), start, start.plusDays(1), start.plusDays(14),
        end.minusDays(1), end, end.plusDays(1), end.plusDays(200),
    )

    private fun java.time.LocalDate.toKotlin() = LocalDate(year, monthValue, dayOfMonth)

    private fun check(key: String, period: BudgetPeriod, start: java.time.LocalDate, end: java.time.LocalDate) {
        val where = "$period '$key'"
        todaysAround(start, end).forEach { today ->
            val (expectedStart, daysLeft) = legacy(start, end, today)
            val progress = budget(key, period).toBudgetProgress(today.toKotlin(), format)
            assertWithMessage("$where today=$today").that(progress).isNotNull()
            assertWithMessage("$where start, today=$today")
                .that(progress!!.periodStart).isEqualTo(expectedStart.toKotlin())
            val expectedPerDay = if (daysLeft > 0) format(left / daysLeft) else null
            assertWithMessage("$where days left, today=$today").that(progress.safePerDay).isEqualTo(expectedPerDay)
        }
    }

    @Test
    fun monthlyPeriodsMatchJavaTime() {
        val names = listOf(
            "January", "February", "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December",
        )
        for (year in 2018..2032) {
            for (month in 1..12) {
                val yearMonth = YearMonth.of(year, month)
                check("${names[month - 1]} $year", BudgetPeriod.MONTHLY, yearMonth.atDay(1), yearMonth.atEndOfMonth())
            }
        }
    }

    @Test
    fun yearlyPeriodsMatchJavaTime() {
        for (year in 2018..2032) {
            val y = Year.of(year)
            check("$year", BudgetPeriod.YEARLY, y.atDay(1), y.atMonth(12).atEndOfMonth())
        }
    }

    @Test
    fun unreadableKeysGiveNoProgress() {
        listOf("", "Octember 2026", "2026-10", "ten").forEach { key ->
            assertWithMessage("monthly '$key'")
                .that(budget(key, BudgetPeriod.MONTHLY).toBudgetProgress(LocalDate(2026, 10, 1), format)).isNull()
        }
        assertWithMessage("yearly 'ten'")
            .that(budget("ten", BudgetPeriod.YEARLY).toBudgetProgress(LocalDate(2026, 10, 1), format)).isNull()
    }
}
