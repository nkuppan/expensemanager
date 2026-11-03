package com.naveenapps.expensemanager.feature.category.transaction

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CategoryTransactionListViewModelTest : BaseCoroutineTest() {

    private val getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase = mock()
    private val appComposeNavigator: AppComposeNavigator = mock()

    private fun stateWith(vararg categoryTransactions: CategoryTransaction) = CategoryTransactionState(
        totalAmount = Amount(0.0),
        pieChartData = emptyList(),
        categoryTransactions = categoryTransactions.toList(),
        hideValues = true,
        categoryType = CategoryType.EXPENSE,
    )

    private fun categoryTransaction(withTransactions: Boolean) = CategoryTransaction(
        category = FAKE_CATEGORY,
        percent = 0f,
        amount = Amount(0.0),
        transaction = if (withTransactions) listOf(FAKE_EXPENSE_TRANSACTION) else emptyList(),
    )

    private suspend fun listedCategories(state: CategoryTransactionState): List<CategoryTransaction> {
        whenever(getTransactionGroupByCategoryUseCase.invoke(any())).thenReturn(flowOf(state))
        val viewModel = CategoryTransactionListViewModel(getTransactionGroupByCategoryUseCase, appComposeNavigator)
        var listed = emptyList<CategoryTransaction>()
        viewModel.state.test {
            var item = awaitItem()
            if (item is UiState.Loading) item = awaitItem()
            listed = (item as UiState.Success).data.categoryTransactions
            cancelAndIgnoreRemainingEvents()
        }
        return listed
    }

    @Test
    fun whenTotalsCannotBeConvertedThenCategoriesWithSpendingAreStillListed() = runTest {
        val listed = listedCategories(stateWith(categoryTransaction(withTransactions = true)))

        Truth.assertThat(listed).hasSize(1)
    }

    @Test
    fun whenThereAreNoTransactionsThenPlaceholderCategoriesAreNotListed() = runTest {
        val listed = listedCategories(stateWith(categoryTransaction(withTransactions = false)))

        Truth.assertThat(listed).isEmpty()
    }
}
