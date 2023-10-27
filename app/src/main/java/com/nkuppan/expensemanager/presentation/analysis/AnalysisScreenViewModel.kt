package com.nkuppan.expensemanager.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.data.utils.toTransactionDateOnly
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.Transaction
import com.nkuppan.expensemanager.domain.model.UiState
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.transaction.GetTransactionsGroupByDateUseCase
import com.nkuppan.expensemanager.presentation.transaction.history.TransactionUIModel
import com.nkuppan.expensemanager.presentation.transaction.history.toTransactionUIModel
import com.nkuppan.expensemanager.ui.utils.UiText
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnalysisScreenViewModel @Inject constructor(
    getCurrencyUseCase: GetCurrencyUseCase,
    getTransactionsGroupByDateUseCase: GetTransactionsGroupByDateUseCase
) : ViewModel() {

    private val _graphItems = MutableStateFlow<UiState<AnalysisData>>(UiState.Loading)
    val graphItems = _graphItems.asStateFlow()

    init {
        getCurrencyUseCase.invoke().combine(
            getTransactionsGroupByDateUseCase.invoke()
        ) { currency, response ->

            response ?: return@combine

            val data = constructGraphItems(
                response,
                currency
            )
            _graphItems.value = if (data == null) UiState.Empty else UiState.Success(data)
        }.launchIn(viewModelScope)
    }
}

data class AnalysisData(
    val transactions: List<TransactionUIModel>,
    val chartData: AnalysisChartData? = null,
)

data class AnalysisChartData(
    val chartData: ChartEntryModel,
    val dates: List<String>,
    val title: UiText? = null,
)

/**
 * @param data it will have data with pair values
 */
suspend fun constructGraphItems(
    data: Map<String, List<Transaction>>?,
    currencySymbol: Currency
): AnalysisData? = withContext(Dispatchers.IO) {

    if (data?.isNotEmpty() == true) {
        val transaction = mutableListOf<TransactionUIModel>()
        val dates = mutableListOf<String>()
        val expenses = mutableListOf<FloatEntry>()
        val incomes = mutableListOf<FloatEntry>()
        var index = 0
        data.forEach { pair ->
            dates.add(pair.value.first().createdOn.toTransactionDateOnly())
            transaction.addAll(pair.value.map { it.toTransactionUIModel(currencySymbol) })
            expenses.add(
                entryOf(
                    index,
                    pair.value.sumOf {
                        if (it.category.type == CategoryType.EXPENSE) -it.amount else 0.0
                    }
                )
            )
            incomes.add(
                entryOf(
                    index,
                    pair.value.sumOf {
                        if (it.category.type == CategoryType.INCOME) it.amount else 0.0
                    }
                )
            )
            index++
        }
        val chartData = ChartEntryModelProducer(listOf(expenses, incomes)).getModel()

        return@withContext AnalysisData(
            transaction,
            AnalysisChartData(
                chartData,
                dates
            )
        )
    }

    return@withContext null
}
