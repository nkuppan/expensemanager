package com.nkuppan.expensemanager.presentation.category.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CategoryTransactionListViewModel @Inject constructor(
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase
) : ViewModel() {
    private val _categoryTransaction = MutableStateFlow<UiState<List<CategoryTransaction>>>(
        UiState.Loading
    )
    val categoryTransaction = _categoryTransaction.asStateFlow()

    init {
        getTransactionGroupByCategoryUseCase.invoke().onEach {
            _categoryTransaction.value = UiState.Success(
                buildList {
                    it.map { add(CategoryTransaction(it.key, it.value)) }
                }
            )
        }.launchIn(viewModelScope)
    }
}

data class CategoryTransaction(
    val category: Category,
    val transaction: List<Transaction> = emptyList()
)