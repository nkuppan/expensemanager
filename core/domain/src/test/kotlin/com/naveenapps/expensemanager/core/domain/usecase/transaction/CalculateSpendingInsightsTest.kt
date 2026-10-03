package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import org.junit.Test

class CalculateSpendingInsightsTest {

    private val zone = ZoneOffset.UTC
    private val food = FAKE_CATEGORY.copy(id = "food", name = "Food")
    private val travel = FAKE_CATEGORY.copy(id = "travel", name = "Travel")
    private val categories = mapOf(food.id to food, travel.id to travel)

    // 2026-09-26 is a Saturday, 2026-09-28 a Monday.
    private val saturday = LocalDate.of(2026, 9, 26)
    private val monday = LocalDate.of(2026, 9, 28)

    private fun tx(
        id: String,
        amount: Double,
        categoryId: String = food.id,
        type: TransactionType = TransactionType.EXPENSE,
        day: LocalDate = monday,
    ) = FAKE_EXPENSE_TRANSACTION.copy(
        id = id,
        categoryId = categoryId,
        type = type,
        amount = Amount(amount),
        createdOn = Date.from(day.atTime(12, 0).toInstant(zone)),
    )

    @Test
    fun `categories are ranked by spend with shares of total expense`() {
        val insights = calculateSpendingInsights(
            listOf(tx("1", 30.0), tx("2", 70.0, travel.id), tx("3", 20.0)),
            categories,
            zone,
        )

        assertThat(insights.totalExpense).isEqualTo(120.0)
        assertThat(insights.categories.map { it.category.id }).containsExactly("travel", "food").inOrder()
        assertThat(insights.categories.first().share).isWithin(0.001f).of(70f / 120f)
        assertThat(insights.categories.last().count).isEqualTo(2)
    }

    @Test
    fun `income and transfers never count as spending`() {
        val insights = calculateSpendingInsights(
            listOf(
                tx("1", 40.0),
                tx("2", 1000.0, type = TransactionType.INCOME),
                tx("3", 500.0, type = TransactionType.TRANSFER),
            ),
            categories,
            zone,
        )

        assertThat(insights.totalExpense).isEqualTo(40.0)
        assertThat(insights.totalIncome).isEqualTo(1000.0)
        assertThat(insights.biggestExpenses.map { it.id }).containsExactly("1")
    }

    @Test
    fun `savings rate is income minus expense over income, null without income`() {
        val withIncome = calculateSpendingInsights(
            listOf(tx("1", 250.0), tx("2", 1000.0, type = TransactionType.INCOME)),
            categories,
            zone,
        )
        val withoutIncome = calculateSpendingInsights(listOf(tx("1", 250.0)), categories, zone)

        assertThat(withIncome.savingsRate).isWithin(0.001f).of(0.75f)
        assertThat(withoutIncome.savingsRate).isNull()
    }

    @Test
    fun `weekdays always list Monday to Sunday and pick the busiest`() {
        val insights = calculateSpendingInsights(
            listOf(tx("1", 10.0, day = monday), tx("2", 90.0, day = saturday)),
            categories,
            zone,
        )

        assertThat(insights.weekdays.map { it.dayOfWeek }).containsExactlyElementsIn(DayOfWeek.entries).inOrder()
        assertThat(insights.busiestWeekday).isEqualTo(DayOfWeek.SATURDAY)
    }

    @Test
    fun `biggest expenses are the largest first, capped`() {
        val transactions = (1..8).map { tx(it.toString(), it * 10.0) }

        val insights = calculateSpendingInsights(transactions, categories, zone, maxBiggest = 3)

        assertThat(insights.biggestExpenses.map { it.amount.amount }).containsExactly(80.0, 70.0, 60.0).inOrder()
    }

    @Test
    fun `no transactions gives empty insights`() {
        val insights = calculateSpendingInsights(emptyList(), categories, zone)

        assertThat(insights.totalExpense).isEqualTo(0.0)
        assertThat(insights.categories).isEmpty()
        assertThat(insights.busiestWeekday).isNull()
        assertThat(insights.weekdays.all { it.total == 0.0 }).isTrue()
    }
}
