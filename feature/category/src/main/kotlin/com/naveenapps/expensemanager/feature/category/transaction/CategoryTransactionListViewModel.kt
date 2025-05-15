package com.naveenapps.expensemanager.feature.category.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CategoryTransactionListViewModel @Inject constructor(
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<CategoryTransactionState>>(
        UiState.Loading,
    )
    val state = _state.asStateFlow()

    private val categoryType = MutableStateFlow(CategoryType.EXPENSE)

    init {
        categoryType.flatMapLatest {
            getTransactionGroupByCategoryUseCase.invoke(it)
        }.onEach { model ->
            _state.value = UiState.Success(
                if (model.hideValues) {
                    model.copy(categoryTransactions = emptyList())
                } else {
                    model
                },
            )
        }.launchIn(viewModelScope)
    }

    private fun switchCategory() {
        this.categoryType.update {
            if (categoryType.value.isExpense()) {
                CategoryType.INCOME
            } else {
                CategoryType.EXPENSE
            }
        }
    }

    private fun openTransactionCreatePage() {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate(null),
        )
    }

    private fun openCategoryDetailsPage(categoryTransaction: CategoryTransaction) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryDetails(categoryTransaction.category.id),
        )
    }

    private fun openCategoryList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.CategoryList)
    }

    fun processAction(action: CategoryTransactionAction) {
        when (action) {
            CategoryTransactionAction.SwitchCategoryType -> {
                switchCategory()
            }

            CategoryTransactionAction.OpenCategoryList -> {
                openCategoryList()
            }

            CategoryTransactionAction.OpenTransactionCreate -> {
                openTransactionCreatePage()
            }

            is CategoryTransactionAction.OpenCategoryDetails -> {
                openCategoryDetailsPage(action.transaction)
            }
        }
    }
}
