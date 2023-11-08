package com.naveenapps.expensemanager.domain.usecase.transaction

import com.naveenapps.expensemanager.data.utils.toCompleteDate
import com.naveenapps.expensemanager.data.utils.toMonthAndYear
import com.naveenapps.expensemanager.data.utils.toYear
import com.naveenapps.expensemanager.domain.model.TransactionUiItem
import com.naveenapps.expensemanager.domain.model.isExpense
import com.naveenapps.expensemanager.domain.model.isIncome
import com.naveenapps.expensemanager.domain.model.toTransactionUIModel
import com.naveenapps.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.domain.usecase.settings.daterange.GetTransactionGroupTypeUseCase
import com.naveenapps.expensemanager.ui.utils.UiText
import com.naveenapps.expensemanager.utils.AppCoroutineDispatchers
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import org.joda.time.DateTime
import java.util.Date
import javax.inject.Inject

class GetChartDataUseCase @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val getTransactionGroupTypeUseCase: GetTransactionGroupTypeUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val dispatcher: AppCoroutineDispatchers,
) {
    fun invoke(): Flow<AnalysisData> {
        return combine(
            getDateRangeUseCase.invoke(),
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke()
        ) { dateRangeModel, currency, transactions ->

            val groupType = getTransactionGroupTypeUseCase.invoke(dateRangeModel.type)

            val transactionGroupByDate = transactions?.groupBy { transaction ->
                return@groupBy groupValue(groupType, transaction.createdOn)
            } ?: emptyMap()

            val ranges = dateRangeModel.dateRanges
            var fromDate = DateTime(ranges[0])
            val toDate = DateTime(ranges[1])


            val transaction = mutableListOf<TransactionUiItem>()
            val dates = mutableListOf<String>()
            val expenses = mutableListOf<FloatEntry>()
            val incomes = mutableListOf<FloatEntry>()
            var index = 0

            while (fromDate < toDate) {
                val key = groupValue(groupType, fromDate.toDate())
                val values = transactionGroupByDate[key]
                dates.add(key)

                if (values != null) {
                    transaction.addAll(values.map { it.toTransactionUIModel(currency) })
                }

                expenses.add(
                    entryOf(
                        index,
                        values?.sumOf {
                            if (it.category.type.isExpense()) it.amount else 0.0
                        } ?: 0.0
                    )
                )
                incomes.add(
                    entryOf(
                        index,
                        values?.sumOf {
                            if (it.category.type.isIncome()) it.amount else 0.0
                        } ?: 0.0
                    )
                )
                fromDate = getAdjustedDateTime(groupType, fromDate)
                index++
            }


            return@combine AnalysisData(
                transaction,
                AnalysisChartData(
                    ChartEntryModelProducer(listOf(expenses, incomes)).getModel(),
                    dates
                )
            )
        }.flowOn(dispatcher.computation)
    }

    private fun getAdjustedDateTime(
        groupType: GroupType,
        fromDate: DateTime
    ) = when (groupType) {
        GroupType.YEAR -> {
            fromDate.plusYears(1)
        }

        GroupType.MONTH -> {
            fromDate.plusMonths(1)
        }

        GroupType.DATE -> {
            fromDate.plusDays(1)
        }
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
    val transactions: List<TransactionUiItem>,
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