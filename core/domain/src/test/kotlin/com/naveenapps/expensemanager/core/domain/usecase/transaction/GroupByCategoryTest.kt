package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertedTotal
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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

    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase = mock {
        onBlocking { currencyFor(any()) }.thenAnswer {
            val code = it.arguments[0] as String
            Currency(symbol = code, name = code, code = code)
        }
    }

    // All entries are in one currency, so no exchange rate is ever looked up.
    private val convertAmountUseCase = ConvertAmountUseCase(mock<ExchangeRateRepository>(), formatAmountInCurrencyUseCase)

    private val format: (ConvertedTotal) -> Amount = { Amount(it.value ?: 0.0, amountString = it.value.toString()) }

    private suspend fun List<Transaction>.group(type: CategoryType) = groupByCategory(type, "USD", convertAmountUseCase, format)

    @Test
    fun groupsByCategoryWithTotalsAndSharesLargestFirst() = runTest {
        val transactions = listOf(
            entry("1", 100.0, food),
            entry("2", 300.0, travel),
            entry("3", 100.0, food),
        )

        val (groups, total) = transactions.group(CategoryType.EXPENSE)

        assertThat(total.value).isEqualTo(500.0)
        assertThat(groups.map { it.category.id }).containsExactly("travel", "food").inOrder()
        assertThat(groups[0].percent).isWithin(0.001f).of(60f)
        assertThat(groups[1].percent).isWithin(0.001f).of(40f)
        assertThat(groups[1].amount.amount).isEqualTo(200.0)
        assertThat(groups[1].transaction.map { it.id }).containsExactly("1", "3")
    }

    @Test
    fun leavesOutTransfersAndTheOtherCategoryType() = runTest {
        val transactions = listOf(
            entry("1", 100.0, food),
            entry("2", 900.0, food, type = TransactionType.TRANSFER),
            entry("3", 5000.0, salary, type = TransactionType.INCOME),
        )

        val (expenses, expenseTotal) = transactions.group(CategoryType.EXPENSE)
        assertThat(expenseTotal.value).isEqualTo(100.0)
        assertThat(expenses.map { it.category.id }).containsExactly("food")

        val (incomes, incomeTotal) = transactions.group(CategoryType.INCOME)
        assertThat(incomeTotal.value).isEqualTo(5000.0)
        assertThat(incomes.single().percent).isWithin(0.001f).of(100f)
    }

    @Test
    fun nothingToGroupGivesAnEmptyResult() = runTest {
        val (groups, total) = emptyList<Transaction>().group(CategoryType.EXPENSE)

        assertThat(groups).isEmpty()
        assertThat(total.value).isEqualTo(0.0)
    }
}
