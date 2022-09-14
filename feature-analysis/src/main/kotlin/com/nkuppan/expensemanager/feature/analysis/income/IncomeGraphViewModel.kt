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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeGraphViewModel @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase
) : ViewModel() {

    private val _errorMessage = Channel<UiText>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _graphItems = MutableStateFlow<List<GraphData>>(emptyList())
    val graphItems = _graphItems.asStateFlow()

    private var currencySymbol: Int = string.default_currency_type

    init {
        viewModelScope.launch {
            getCurrencyUseCase.invoke().collectLatest {
                currencySymbol = it.type
            }
        }
    }

    fun loadIncomeData() {
        viewModelScope.launch {
            when (val response = getTransactionGroupByCategoryUseCase.invoke()) {
                is Resource.Error -> {
                    _errorMessage.send(UiText.StringResource(R.string.unable_to_load_graph_items))
                }
                is Resource.Success -> {
                    _graphItems.value = constructGraphItems(
                        response.data,
                        CategoryType.INCOME,
                        currencySymbol
                    )
                }
            }
        }
    }
}