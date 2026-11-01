package com.naveenapps.expensemanager.feature.dashboard

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.BudgetTitle
import com.naveenapps.expensemanager.feature.budget.components.toBudgetProgress
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Test

class HomeBudgetTest {

    // Month keys are parsed in the device time zone, so the period maths uses the same zone.
    private val zone = ZoneId.systemDefault()
    private val format: (Double) -> String = { "%.0f".format(it) }

    private fun budget(
        id: String,
        limit: Double,
        spent: Double = 0.0,
        period: BudgetPeriod = BudgetPeriod.MONTHLY,
        key: String = "October 2026",
    ) = BudgetUiModel(
        id = id,
        title = BudgetTitle.ThisMonth,
        selectedMonth = key,
        periodType = period,
        progressBarColor = 0,
        amount = Amount(limit, "₹$limit"),
        transactionAmount = Amount(spent, "₹$spent"),
        percent = (spent / limit * 100).toFloat(),
    )

    @Test
    fun `home shows every budget for the viewed month and this year, largest first`() {
        val small = budget("groceries", 5_000.0)
        val overall = budget("overall", 25_000.0)
        val transport = budget("transport", 3_000.0)
        val lastMonth = budget("september", 30_000.0, key = "September 2026")
        val yearly = budget("year", 300_000.0, period = BudgetPeriod.YEARLY, key = "2026")
        val travel = budget("travel", 60_000.0, period = BudgetPeriod.YEARLY, key = "2026")
        val lastYear = budget("2025", 250_000.0, period = BudgetPeriod.YEARLY, key = "2025")

        val (monthly, annual) = pickHomeBudgets(
            listOf(small, overall, transport, lastMonth, yearly, travel, lastYear),
            "October 2026",
            "2026",
        )

        assertThat(monthly.map { it.id }).containsExactly("overall", "groceries", "transport").inOrder()
        assertThat(annual.map { it.id }).containsExactly("year", "travel").inOrder()
    }

    @Test
    fun `a single budget for a period is the only card`() {
        val (monthly, annual) = pickHomeBudgets(listOf(budget("overall", 25_000.0)), "October 2026", "2026")

        assertThat(monthly.map { it.id }).containsExactly("overall")
        assertThat(annual).isEmpty()
    }

    @Test
    fun `no budget for a period leaves that tab empty`() {
        val (monthly, annual) = pickHomeBudgets(listOf(budget("overall", 25_000.0)), "November 2026", "2026")

        assertThat(monthly).isEmpty()
        assertThat(annual).isEmpty()
    }

    @Test
    fun `no month in view shows no monthly budgets`() {
        val (monthly, _) = pickHomeBudgets(listOf(budget("overall", 25_000.0)), null, "2026")

        assertThat(monthly).isEmpty()
    }

    @Test
    fun `remaining and safe to spend per day count today and the rest of the month`() {
        // 29 October: 3 days left (29, 30, 31).
        val home = budget("overall", 25_000.0, spent = 50.0)
            .toBudgetProgress(LocalDate.of(2026, 10, 29), format, zone)!!

        assertThat(home.remaining).isEqualTo("24950")
        assertThat(home.isOverBudget).isFalse()
        assertThat(home.safePerDay).isEqualTo("8317")
        assertThat(home.progress).isWithin(0.0001f).of(0.002f)
        assertThat(home.periodStart).isEqualTo(LocalDate.of(2026, 10, 1))
    }

    @Test
    fun `an overspent budget shows how much it's over and no daily amount`() {
        val home = budget("overall", 1_000.0, spent = 1_250.0)
            .toBudgetProgress(LocalDate.of(2026, 10, 29), format, zone)!!

        assertThat(home.isOverBudget).isTrue()
        assertThat(home.remaining).isEqualTo("250")
        assertThat(home.safePerDay).isNull()
        assertThat(home.progress).isEqualTo(1f)
    }

    @Test
    fun `a yearly budget spreads what's left over the rest of the year`() {
        // 30 December: 2 days left.
        val home = budget("year", 12_000.0, spent = 10_000.0, period = BudgetPeriod.YEARLY, key = "2026")
            .toBudgetProgress(LocalDate.of(2026, 12, 30), format, zone)!!

        assertThat(home.safePerDay).isEqualTo("1000")
        assertThat(home.periodStart).isEqualTo(LocalDate.of(2026, 1, 1))
    }

    @Test
    fun `a period that has ended has no daily amount and a future one uses all its days`() {
        val past = budget("old", 1_000.0, key = "September 2026")
            .toBudgetProgress(LocalDate.of(2026, 10, 29), format, zone)!!
        val future = budget("next", 3_000.0, key = "November 2026")
            .toBudgetProgress(LocalDate.of(2026, 10, 29), format, zone)!!

        assertThat(past.safePerDay).isNull()
        assertThat(future.safePerDay).isEqualTo("100")
    }
}
