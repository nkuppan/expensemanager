package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.changeRatio
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import org.junit.Test

class CalculatePeriodComparisonTest {

    private val food = FAKE_CATEGORY.copy(id = "food", name = "Food")
    private val travel = FAKE_CATEGORY.copy(id = "travel", name = "Travel")
    private val rent = FAKE_CATEGORY.copy(id = "rent", name = "Rent")
    private val categories = listOf(food, travel, rent).associateBy { it.id }
    private val windows = ComparisonWindows(DateRangeType.THIS_MONTH, 0, 10, -10, 0, isToDate = true)

    private fun tx(amount: Double, categoryId: String = food.id, type: TransactionType = TransactionType.EXPENSE) =
        FAKE_EXPENSE_TRANSACTION.copy(categoryId = categoryId, type = type, amount = Amount(amount))

    @Test
    fun `totals are split by type for both periods`() {
        val result = calculatePeriodComparison(
            PeriodTransactions(
                windows,
                current = listOf(tx(30.0), tx(20.0), tx(500.0, type = TransactionType.INCOME)),
                previous = listOf(tx(40.0), tx(400.0, type = TransactionType.INCOME)),
            ),
            categories,
        )

        assertThat(result.currentExpense).isEqualTo(50.0)
        assertThat(result.previousExpense).isEqualTo(40.0)
        assertThat(result.currentIncome).isEqualTo(500.0)
        assertThat(result.previousIncome).isEqualTo(400.0)
        assertThat(result.isToDate).isTrue()
    }

    @Test
    fun `biggest category movers come first, unchanged ones are dropped`() {
        val result = calculatePeriodComparison(
            PeriodTransactions(
                windows,
                current = listOf(tx(10.0, food.id), tx(300.0, travel.id), tx(100.0, rent.id)),
                previous = listOf(tx(60.0, food.id), tx(100.0, rent.id)),
            ),
            categories,
        )

        assertThat(result.categoryChanges.map { it.category.id }).containsExactly("travel", "food").inOrder()
        assertThat(result.categoryChanges.first().difference).isEqualTo(300.0)
        assertThat(result.categoryChanges.last().difference).isEqualTo(-50.0)
    }

    @Test
    fun `change ratio needs a previous value`() {
        assertThat(changeRatio(120.0, 100.0)).isWithin(1e-9).of(0.2)
        assertThat(changeRatio(50.0, 0.0)).isNull()
    }
}
