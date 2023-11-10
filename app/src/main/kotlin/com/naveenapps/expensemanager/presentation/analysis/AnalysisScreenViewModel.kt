package com.naveenapps.expensemanager.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.domain.model.AverageData
import com.naveenapps.expensemanager.domain.model.WholeAverageData
import com.naveenapps.expensemanager.domain.usecase.transaction.AnalysisData
import com.naveenapps.expensemanager.domain.usecase.transaction.GetAmountStateUseCase
import com.naveenapps.expensemanager.domain.usecase.transaction.GetAverageDataUseCase
import com.naveenapps.expensemanager.domain.usecase.transaction.GetChartDataUseCase
import com.naveenapps.expensemanager.presentation.dashboard.AmountUiState
import com.naveenapps.expensemanager.ui.utils.getCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AnalysisScreenViewModel @Inject constructor(
    getChartDataUseCase: GetChartDataUseCase,
    getAverageDataUseCase: GetAverageDataUseCase,
    getAmountStateUseCase: GetAmountStateUseCase,
) : ViewModel() {

    private val currency = Currency(
        com.naveenapps.expensemanager.core.data.R.string.dollar_type,
        com.naveenapps.expensemanager.core.data.R.string.dollar_name,
        com.naveenapps.expensemanager.core.data.R.drawable.currency_dollar
    )

    private val _amountUiState = MutableStateFlow(AmountUiState())
    val amountUiState = _amountUiState.asStateFlow()

    private val _graphItems = MutableStateFlow<UiState<AnalysisData>>(UiState.Loading)
    val graphItems = _graphItems.asStateFlow()

    private val _averageData = MutableStateFlow(
        WholeAverageData(
            AverageData(
                getCurrency(currency, 0.0),
                getCurrency(currency, 0.0),
                getCurrency(currency, 0.0),
            ),
            AverageData(
                getCurrency(currency, 0.0),
                getCurrency(currency, 0.0),
                getCurrency(currency, 0.0),
            )
        )
    )
    val averageData = _averageData.asStateFlow()

    init {
        getChartDataUseCase.invoke().onEach { response ->
            _graphItems.value = UiState.Success(response)
        }.launchIn(viewModelScope)

        getAverageDataUseCase.invoke().onEach { response ->
            _averageData.value = response
        }.launchIn(viewModelScope)

        getAmountStateUseCase.invoke().onEach { response ->
            _amountUiState.value = response
        }.launchIn(viewModelScope)
    }
}
