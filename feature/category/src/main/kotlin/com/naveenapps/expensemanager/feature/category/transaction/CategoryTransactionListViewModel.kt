package com.naveenapps.expensemanager.feature.category.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.core.model.CategoryTransactionUiModel
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CategoryTransactionListViewModel @Inject constructor(
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
    private val appComposeNavigator: AppComposeNavigator
) : ViewModel() {
    private val _categoryTransaction = MutableStateFlow<UiState<CategoryTransactionUiModel>>(
        UiState.Loading
    )
    val categoryTransaction = _categoryTransaction.asStateFlow()

    private val _categoryType = MutableStateFlow(CategoryType.EXPENSE)
    val categoryType = _categoryType.asStateFlow()

    init {
        categoryType.flatMapMerge {
            getTransactionGroupByCategoryUseCase.invoke(it)
        }.onEach { model ->
            _categoryTransaction.value = UiState.Success(
                if (model.hideValues) {
                    model.copy(
                        categoryTransactions = emptyList()
                    )
                } else {
                    model
                }
            )
        }.launchIn(viewModelScope)
    }

    fun setCategoryType(categoryType: CategoryType) {
        this._categoryType.value = categoryType
    }

    fun openCreatePage() {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.TransactionCreate.createRoute("")
        )
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}