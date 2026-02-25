package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetTransactionGroupTypeUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import java.util.Date
import kotlin.time.ExperimentalTime

class GetChartDataUseCase(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val getTransactionGroupTypeUseCase: GetTransactionGroupTypeUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val dispatcher: AppCoroutineDispatchers,
) {
    @OptIn(ExperimentalTime::class)
    fun invoke(): Flow<AnalysisData> {
        return combine(
            getDateRangeUseCase.invoke(),
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
        ) { dateRangeModel, currency, transactions ->

            val groupType = getTransactionGroupTypeUseCase.invoke(dateRangeModel.type)

            val transactionGroupByDate = transactions?.groupBy {
                groupValue(groupType, it.createdOn)
            } ?: emptyMap()

            val ranges = dateRangeModel.dateRanges
            var fromDate = kotlin.time.Instant.fromEpochMilliseconds(ranges[0])
            val toDate = kotlin.time.Instant.fromEpochMilliseconds(ranges[1])

            val transactionItems = ArrayList<TransactionUiItem>(transactions?.size ?: 0)
            val dates = mutableListOf<String>()
            val expenses = mutableListOf<FloatEntryModel>()
            val incomes = mutableListOf<FloatEntryModel>()
            var index = 0

            val zeroFormatted = getFormattedAmountUseCase.invoke(0.0, currency)

            while (fromDate < toDate) {
                val key = groupValue(groupType, fromDate.toEpochMilliseconds().toCompleteDate())
                val currentValues = transactionGroupByDate[key]

                dates.add(key)

                var totalExpense = 0.0
                var totalIncome = 0.0

                // 5. SINGLE PASS: Calculate totals and map UI items at the same time
                currentValues?.forEach { item ->
                    val amount = item.amount.amount

                    // Accumulate totals
                    if (item.category.type.isExpense()) {
                        totalExpense += amount
                    } else if (item.category.type.isIncome()) {
                        totalIncome += amount
                    }

                    // Add to the main transaction list
                    transactionItems.add(
                        item.toTransactionUIModel(
                            getFormattedAmountUseCase.invoke(amount, currency)
                        )
                    )
                }

                // 6. Add to chart data (Only format final totals)
                expenses.add(
                    FloatEntryModel(
                        index,
                        totalExpense,
                        if (totalExpense == 0.0) zeroFormatted else getFormattedAmountUseCase.invoke(
                            totalExpense,
                            currency
                        )
                    )
                )
                incomes.add(
                    FloatEntryModel(
                        index,
                        totalIncome,
                        if (totalIncome == 0.0) zeroFormatted else getFormattedAmountUseCase.invoke(
                            totalIncome,
                            currency
                        )
                    )
                )

                // Move to next period
                fromDate = getAdjustedDateTime(groupType, fromDate)
                index++
            }

            return@combine AnalysisData(
                transactionItems,
                if (transactionGroupByDate.isEmpty()) null
                else AnalysisChartData(listOf(expenses, incomes), dates)
            )
        }.flowOn(dispatcher.computation)
    }

    @OptIn(ExperimentalTime::class)
    private fun getAdjustedDateTime(
        groupType: GroupType,
        fromDate: kotlin.time.Instant,
    ) = when (groupType) {
        GroupType.YEAR -> {
            fromDate.plus(1, DateTimeUnit.YEAR, TimeZone.UTC)
        }

        GroupType.MONTH -> {
            fromDate.plus(1, DateTimeUnit.MONTH, TimeZone.UTC)
        }

        GroupType.DATE -> {
            fromDate.plus(1, DateTimeUnit.DAY, TimeZone.UTC)
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
            transactionDate.toCompleteDateWithDate()
        }
    }
}

data class FloatEntryModel(
    val index: Int,
    val total: Double,
    val amountString: Amount,
)

data class AnalysisData(
    val transactions: List<TransactionUiItem>,
    val chartData: AnalysisChartData? = null,
)

data class AnalysisChartData(
    val chartData: List<MutableList<FloatEntryModel>>,
    val dates: List<String>,
    val title: String? = null,
)
