package com.nkuppan.expensemanager.presentation.analysis.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.presentation.analysis.expense.GraphData
import com.nkuppan.expensemanager.presentation.analysis.expense.constructGraphItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class IncomeGraphViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _graphItems = MutableStateFlow<List<GraphData>>(emptyList())
    val graphItems = _graphItems.asStateFlow()

    private var currencySymbol: Int = R.string.default_currency_type

    init {

        getCurrencyUseCase.invoke().onEach {
            currencySymbol = it.type
        }.launchIn(viewModelScope)

        getTransactionGroupByCategoryUseCase.invoke().onEach { response ->
            _graphItems.value = constructGraphItems(
                response,
                CategoryType.EXPENSE,
                currencySymbol
            )
        }.launchIn(viewModelScope)
    }
}