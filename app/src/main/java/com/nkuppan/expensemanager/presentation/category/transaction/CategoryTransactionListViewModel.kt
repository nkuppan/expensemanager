package com.nkuppan.expensemanager.presentation.category.transaction

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.nkuppan.expensemanager.ui.utils.UiText
import com.nkuppan.expensemanager.ui.utils.getCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class CategoryTransactionListViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
) : ViewModel() {
    private val _categoryTransaction = MutableStateFlow<UiState<CategoryTransactionUiModel>>(
        UiState.Loading
    )
    val categoryTransaction = _categoryTransaction.asStateFlow()

    private val _categoryType = MutableStateFlow(CategoryType.EXPENSE)
    val categoryType = _categoryType.asStateFlow()

    init {
        combine(
            categoryType,
            getCurrencyUseCase.invoke(),
            getTransactionGroupByCategoryUseCase.invoke()
        ) { categoryType, currency, transaction ->

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
            }.sortedByDescending { it.percent }

            val pieChartData = if (categoryTransactions.isEmpty()) {
                listOf(
                    getDummyPieChartData(),
                    getDummyPieChartData(),
                    getDummyPieChartData(),
                    getDummyPieChartData()
                )
            } else {
                categoryTransactions.map {
                    it.toChartModel()
                }
            }
            _categoryTransaction.value =
                UiState.Success(
                    CategoryTransactionUiModel(
                        pieChartData = pieChartData,
                        totalAmount = getCurrency(
                            currency = currency,
                            amount = totalAmount
                        ),
                        categoryTransactions = categoryTransactions,
                        hideValues = categoryTransactions.isEmpty()
                    )
                )
        }.launchIn(viewModelScope)
    }

    fun setCategoryType(categoryType: CategoryType) {
        this._categoryType.value = categoryType
    }
}

fun CategoryTransaction.toChartModel(): PieChartData {
    return PieChartData(
        name = this.category.name,
        value = this.percent,
        color = this.category.iconBackgroundColor.toColorInt(),
    )
}

fun getDummyPieChartData(): PieChartData {
    return PieChartData(
        name = "",
        value = 25.0f,
        color = Color.parseColor("#30000000"),
    )
}

data class PieChartData(
    var name: String,
    var value: Float,
    @ColorInt var color: Int,
)

data class CategoryTransaction(
    val category: Category,
    val percent: Float,
    val amount: UiText,
    val transaction: List<Transaction> = emptyList()
)

data class CategoryTransactionUiModel(
    val totalAmount: UiText,
    val pieChartData: List<PieChartData>,
    val categoryTransactions: List<CategoryTransaction>,
    val hideValues: Boolean = false
)