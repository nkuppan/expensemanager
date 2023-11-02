package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.data.utils.toCompleteDate
import com.nkuppan.expensemanager.data.utils.toMonthAndYear
import com.nkuppan.expensemanager.data.utils.toYear
import com.nkuppan.expensemanager.domain.model.CategoryType
import com.nkuppan.expensemanager.domain.model.TransactionUIModel
import com.nkuppan.expensemanager.domain.model.toTransactionUIModel
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetDateRangeFilterTypeUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetTransactionGroupTypeUseCase
import com.nkuppan.expensemanager.ui.utils.UiText
import com.nkuppan.expensemanager.utils.AppCoroutineDispatchers
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.util.Date
import javax.inject.Inject

class GetChartDataUseCase @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getDateRangeFilterTypeUseCase: GetDateRangeFilterTypeUseCase,
    private val getTransactionGroupTypeUseCase: GetTransactionGroupTypeUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val dispatcher: AppCoroutineDispatchers,
) {
    fun invoke(): Flow<AnalysisData> {
        return combine(
            getDateRangeFilterTypeUseCase.invoke(),
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke()
        ) { filterType, currency, transactions ->

            val groupType = getTransactionGroupTypeUseCase(filterType)

            val transactionGroupByDate = transactions?.groupBy { transaction ->
                return@groupBy groupValue(groupType, transaction.createdOn)
            } ?: emptyMap()

            val data = if (transactionGroupByDate.isNotEmpty()) {
                val transaction = mutableListOf<TransactionUIModel>()
                val dates = mutableListOf<String>()
                val expenses = mutableListOf<FloatEntry>()
                val incomes = mutableListOf<FloatEntry>()
                var index = 0
                transactionGroupByDate.forEach { pair ->
                    dates.add(pair.key)
                    transaction.addAll(pair.value.map { it.toTransactionUIModel(currency) })
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

                AnalysisData(
                    transaction,
                    AnalysisChartData(
                        ChartEntryModelProducer(listOf(expenses, incomes)).getModel(),
                        dates
                    )
                )
            } else {
                AnalysisData(
                    transactions = emptyList()
                )
            }

            return@combine data
        }.flowOn(dispatcher.computation)
    }

    private fun groupValue(groupType: GroupType, transactionDate: Date) = when (groupType) {
        GroupType.YEAR -> {
            transactionDate.toYear()
        }

        GroupType.MONTH -> {
            transactionDate.toMonthAndYear()
        }

        GroupType.DATE -> {
            transactionDate.toCompleteDate()
        }
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

enum class GroupType {
    YEAR,
    MONTH,
    DATE
}