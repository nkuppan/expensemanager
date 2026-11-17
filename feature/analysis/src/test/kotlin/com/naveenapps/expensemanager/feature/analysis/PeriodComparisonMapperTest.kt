package com.naveenapps.expensemanager.feature.analysis

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.CategoryChange
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.PeriodComparison
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import org.junit.Test

class PeriodComparisonMapperTest {

    private val zone = ZoneOffset.UTC
    private val format: (Double) -> String = { "₹" + it.toInt() }

    private fun millis(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day).atStartOfDay(zone).toInstant().toEpochMilli()

    private fun comparison(
        type: DateRangeType = DateRangeType.THIS_MONTH,
        currentExpense: Double = 300.0,
        previousExpense: Double = 200.0,
        currentIncome: Double = 0.0,
        previousIncome: Double = 0.0,
        categoryChanges: List<CategoryChange> = emptyList(),
    ) = PeriodComparison(
        type = type,
        isToDate = true,
        // 1–3 Oct vs 1–3 Sep (ends are exclusive).
        currentStart = millis(2026, 10, 1),
        currentEnd = millis(2026, 10, 4),
        previousStart = millis(2026, 9, 1),
        previousEnd = millis(2026, 9, 4),
        currentExpense = currentExpense,
        previousExpense = previousExpense,
        currentIncome = currentIncome,
        previousIncome = previousIncome,
        categoryChanges = categoryChanges,
    )

    @Test
    fun nothingInEitherPeriodShowsNoCard() {
        val empty = comparison(currentExpense = 0.0, previousExpense = 0.0)

        assertThat(empty.toUi(zone, Locale.ENGLISH, format)).isNull()
    }

    @Test
    fun labelsBothPeriodsAndFormatsTheTotals() {
        val ui = comparison().toUi(zone, Locale.ENGLISH, format)!!

        assertThat(ui.titleResId).isEqualTo(R.string.compare_title_month)
        assertThat(ui.periodsLabel).isEqualTo("1–3 Oct vs 1–3 Sep")
        assertThat(ui.expense.current).isEqualTo("₹300")
        assertThat(ui.expense.previous).isEqualTo("₹200")
        assertThat(ui.expense.change).isWithin(0.0001).of(0.5)
        // No income last time: no percentage to show.
        assertThat(ui.income.change).isNull()
    }

    @Test
    fun categoryChangesAreSignedWithTheirDirection() {
        val food = FAKE_CATEGORY.copy(id = "food")
        val travel = FAKE_CATEGORY.copy(id = "travel")
        val ui = comparison(
            categoryChanges = listOf(
                CategoryChange(food, current = 500.0, previous = 200.0),
                CategoryChange(travel, current = 50.0, previous = 100.0),
            ),
        ).toUi(zone, Locale.ENGLISH, format)!!

        assertThat(ui.categoryChanges[0].difference).isEqualTo("+₹300")
        assertThat(ui.categoryChanges[0].isIncrease).isTrue()
        assertThat(ui.categoryChanges[1].difference).isEqualTo("−₹50")
        assertThat(ui.categoryChanges[1].isIncrease).isFalse()
    }

    @Test
    fun yearlyComparisonsIncludeTheYear() {
        val ui = comparison(type = DateRangeType.THIS_YEAR).toUi(zone, Locale.ENGLISH, format)!!

        assertThat(ui.titleResId).isEqualTo(R.string.compare_title_year)
        assertThat(ui.periodsLabel).isEqualTo("1–3 Oct 2026 vs 1–3 Sep 2026")
    }
}
