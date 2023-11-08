package com.naveenapps.expensemanager.presentation.category.transaction

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.domain.model.Category
import com.naveenapps.expensemanager.domain.model.CategoryType
import com.naveenapps.expensemanager.domain.model.Transaction
import com.naveenapps.expensemanager.domain.model.UiState
import com.naveenapps.expensemanager.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.ui.utils.UiText
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
}

fun CategoryTransaction.toChartModel(): PieChartData {
    return PieChartData(
        name = this.category.name,
        value = this.percent,
        color = this.category.iconBackgroundColor.toColorInt(),
    )
}

fun getDummyPieChartData(categoryName: String, percent: Float): PieChartData {
    return PieChartData(
        name = categoryName,
        value = percent,
        color = Color.parseColor("#40121212"),
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