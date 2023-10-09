package com.nkuppan.expensemanager.presentation.category.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.common.ui.utils.UiText
import com.nkuppan.expensemanager.common.ui.utils.getCurrency
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class CategoryTransactionListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
) : ViewModel() {
    private val _categoryTransaction = MutableStateFlow<UiState<List<CategoryTransaction>>>(
        UiState.Loading
    )
    val categoryTransaction = _categoryTransaction.asStateFlow()

    private var categoryType: CategoryType = CategoryType.EXPENSE

    init {

        categoryType = CategoryType.values()[savedStateHandle.get<Int>("categoryType")
            ?: CategoryType.EXPENSE.ordinal]

        combine(
            getCurrencyUseCase.invoke(),
            getTransactionGroupByCategoryUseCase.invoke()
        ) { currency, transaction ->

            val filteredTransaction = transaction.filter { it.key.type == categoryType }

            val totalAmount =
                filteredTransaction.map { it.value.sumOf { transactions -> transactions.amount } }
                    .sumOf { it }

            val categoryTransactions = buildList {
                filteredTransaction.map {
                    val totalSpentAmount = it.value.sumOf {
                        it.amount
                    }
                    add(
                        CategoryTransaction(
                            it.key,
                            percent = (totalSpentAmount / totalAmount).toFloat() * 100,
                            getCurrency(
                                currency = currency,
                                amount = totalSpentAmount
                            ),
                            it.value
                        )
                    )
                }
            }

            _categoryTransaction.value =
                UiState.Success(categoryTransactions.sortedByDescending { it.percent })
        }.launchIn(viewModelScope)
    }
}

data class CategoryTransaction(
    val category: Category,
    val percent: Float,
    val amount: UiText,
    val transaction: List<Transaction> = emptyList()
)