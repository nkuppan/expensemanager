package com.naveenapps.expensemanager.feature.analysis

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetAmountStateUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetAverageDataUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetChartDataUseCase
import com.naveenapps.expensemanager.core.model.AmountUiState
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.WholeAverageData
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AnalysisScreenViewModel @Inject constructor(
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
    getChartDataUseCase: GetChartDataUseCase,
    getAverageDataUseCase: GetAverageDataUseCase,
    getAmountStateUseCase: GetAmountStateUseCase,
) : ViewModel() {


    private val _currentTheme = MutableStateFlow(
        Theme(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            R.string.analysis
        )
    )
    val currentTheme = _currentTheme.asStateFlow()

    private val _amountUiState = MutableStateFlow(AmountUiState())
    val amountUiState = _amountUiState.asStateFlow()

    private val _transactionPeriod = MutableStateFlow("")
    val transactionPeriod = _transactionPeriod.asStateFlow()

    private val _graphItems = MutableStateFlow<UiState<AnalysisUiData>>(UiState.Loading)
    val graphItems = _graphItems.asStateFlow()

    private val _averageData = MutableStateFlow(
        WholeAverageData(
            AverageData(
                "0.00$",
                "0.00$",
                "0.00$",
            ),
            AverageData(
                "0.00$",
                "0.00$",
                "0.00$",
            )
        )
    )
    val averageData = _averageData.asStateFlow()

    init {
        getChartDataUseCase.invoke().onEach { response ->
            _graphItems.value = UiState.Success(
                AnalysisUiData(
                    transactions = response.transactions,
                    chartData = response.chartData?.let { chart ->
                        AnalysisUiChartData(
                            chartData = ChartEntryModelProducer(
                                chart.chartData.map {
                                    it.map { entry ->
                                        entryOf(
                                            entry.index,
                                            entry.total
                                        )
                                    }
                                }
                            ).getModel(),
                            dates = chart.dates
                        )
                    }
                )
            )
        }.launchIn(viewModelScope)

        getAverageDataUseCase.invoke().onEach { response ->
            _averageData.value = response
        }.launchIn(viewModelScope)

        getAmountStateUseCase.invoke().onEach { response ->
            _amountUiState.value = response
        }.launchIn(viewModelScope)

        getCurrentThemeUseCase.invoke().onEach {
            _currentTheme.value = it
        }.launchIn(viewModelScope)
    }
}


data class AnalysisUiData(
    val transactions: List<TransactionUiItem>,
    val chartData: AnalysisUiChartData? = null,
)

data class AnalysisUiChartData(
    val chartData: ChartEntryModel,
    val dates: List<String>,
    val title: String? = null,
)
