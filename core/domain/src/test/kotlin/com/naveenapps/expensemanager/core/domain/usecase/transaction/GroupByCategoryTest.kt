package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import org.junit.Test

class GroupByCategoryTest {

    private val food = FAKE_CATEGORY.copy(id = "food", name = "Food")
    private val travel = FAKE_CATEGORY.copy(id = "travel", name = "Travel")
    private val salary = FAKE_CATEGORY.copy(id = "salary", name = "Salary", type = CategoryType.INCOME)

    private fun entry(
        id: String,
        amount: Double,
        category: Category,
        type: TransactionType = TransactionType.EXPENSE,
    ): Transaction = FAKE_EXPENSE_TRANSACTION.copy(
        id = id,
        amount = Amount(amount),
        categoryId = category.id,
        category = category,
        type = type,
    )

    private val format: (Double) -> Amount = { Amount(it, amountString = it.toString()) }

    @Test
    fun groupsByCategoryWithTotalsAndSharesLargestFirst() {
        val transactions = listOf(
            entry("1", 100.0, food),
            entry("2", 300.0, travel),
            entry("3", 100.0, food),
        )

        val (groups, total) = transactions.groupByCategory(CategoryType.EXPENSE, format)

        assertThat(total).isEqualTo(500.0)
        assertThat(groups.map { it.category.id }).containsExactly("travel", "food").inOrder()
        assertThat(groups[0].percent).isWithin(0.001f).of(60f)
        assertThat(groups[1].percent).isWithin(0.001f).of(40f)
        assertThat(groups[1].amount.amount).isEqualTo(200.0)
        assertThat(groups[1].transaction.map { it.id }).containsExactly("1", "3")
    }

    @Test
    fun refundsReduceTheirCategorySpending() {
        val transactions = listOf(
            entry("1", 200.0, food),
            entry("2", 300.0, travel),
            entry("3", 60.0, food, type = TransactionType.REFUND),
        )

        val (groups, total) = transactions.groupByCategory(CategoryType.EXPENSE, format)

        assertThat(total).isEqualTo(440.0)
        assertThat(groups.first { it.category.id == food.id }.amount.amount).isEqualTo(140.0)
        assertThat(groups.first { it.category.id == travel.id }.amount.amount).isEqualTo(300.0)
    }

    @Test
    fun leavesOutTransfersAndTheOtherCategoryType() {
        val transactions = listOf(
            entry("1", 100.0, food),
            entry("2", 900.0, food, type = TransactionType.TRANSFER),
            entry("3", 5000.0, salary, type = TransactionType.INCOME),
        )

        val (expenses, expenseTotal) = transactions.groupByCategory(CategoryType.EXPENSE, format)
        assertThat(expenseTotal).isEqualTo(100.0)
        assertThat(expenses.map { it.category.id }).containsExactly("food")

        val (incomes, incomeTotal) = transactions.groupByCategory(CategoryType.INCOME, format)
        assertThat(incomeTotal).isEqualTo(5000.0)
        assertThat(incomes.single().percent).isWithin(0.001f).of(100f)
    }

    @Test
    fun nothingToGroupGivesAnEmptyResult() {
        val (groups, total) = emptyList<Transaction>().groupByCategory(CategoryType.EXPENSE, format)

        assertThat(groups).isEmpty()
        assertThat(total).isEqualTo(0.0)
    }
}
