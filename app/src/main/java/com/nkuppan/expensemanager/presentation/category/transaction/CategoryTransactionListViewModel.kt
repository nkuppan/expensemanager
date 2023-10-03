package com.nkuppan.expensemanager.presentation.category.transaction

import androidx.lifecycle.ViewModel
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionWithFilterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CategoryTransactionListViewModel @Inject constructor(
    val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase
) : ViewModel() {
    private val _categoryTransaction =
        MutableStateFlow<UiState<List<CategoryTransaction>>>(UiState.Loading)
    val categoryTransaction = _categoryTransaction.asStateFlow()
}

data class CategoryTransaction(
    val category: Category,
    val transaction: List<Transaction> = emptyList()
)