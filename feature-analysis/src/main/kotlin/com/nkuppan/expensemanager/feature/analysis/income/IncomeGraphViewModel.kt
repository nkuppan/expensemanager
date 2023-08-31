package com.nkuppan.expensemanager.feature.analysis.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.CategoryType
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.data.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.data.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.nkuppan.expensemanager.feature.analysis.R
import com.nkuppan.expensemanager.feature.analysis.expense.GraphData
import com.nkuppan.expensemanager.feature.analysis.expense.constructGraphItems
import com.nkuppan.expensemanager.feature.transaction.R.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    private var currencySymbol: Int = com.nkuppan.expensemanager.data.R.string.default_currency_type

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