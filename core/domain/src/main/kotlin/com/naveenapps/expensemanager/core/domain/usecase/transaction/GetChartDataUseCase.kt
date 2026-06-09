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
import com.naveenapps.expensemanager.core.model.DateRangeType
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
import kotlinx.datetime.minus
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

            if (transactions.isNullOrEmpty()) {
                return@combine AnalysisData(emptyList(), null)
            }

            val isAllTime = dateRangeModel.type == DateRangeType.ALL

            // For "All time", always group by year regardless of the repository's suggestion.
            // ranges[1] is Long.MAX_VALUE for ALL, which would make the while loop iterate
            // from 1970 to year ~292 million. Forcing YEAR and clamping both ends to actual
            // transaction dates keeps the chart point count equal to the number of distinct
            // years in the data — naturally bounded, no hard cap needed.
            val groupType = if (isAllTime) GroupType.YEAR
            else getTransactionGroupTypeUseCase.invoke(dateRangeModel.type)

            val zeroFormatted = getFormattedAmountUseCase.invoke(0.0, currency)

            // Single pass: aggregate totals per date bucket AND build UI items simultaneously.
            // DoubleArray(2) holds [totalExpense, totalIncome] per bucket — avoids the
            // groupBy() call that previously created a HashMap<String, MutableList<Transaction>>,
            // which doubled peak memory by keeping a second reference-structure over all
            // transactions alive alongside the original list.
            val bucketTotals = HashMap<String, DoubleArray>(minOf(transactions.size, MAX_CHART_POINTS))
            val transactionItems = ArrayList<TransactionUiItem>(transactions.size)

            transactions.forEach { item ->
                val key = groupValue(groupType, item.createdOn)
                val bucket = bucketTotals.getOrPut(key) { DoubleArray(2) }
                val amount = item.amount.amount
                when {
                    item.category.type.isExpense() -> bucket[0] += amount
                    item.category.type.isIncome() -> bucket[1] += amount
                }
                transactionItems.add(
                    item.toTransactionUIModel(getFormattedAmountUseCase.invoke(amount, currency))
                )
            }

            val ranges = dateRangeModel.dateRanges
            val earliestTransactionMs = transactions.minOf { it.createdOn.time }
            val latestTransactionMs = transactions.maxOf { it.createdOn.time }

            // For ALL: extend one year before the earliest transaction and one year after
            // the latest, so the chart shows an empty bucket on each side for visual context.
            // For other ranges: clamp only the start (keeps empty trailing buckets visible
            // for ranges the user explicitly selected).
            val effectiveFromMs = if (isAllTime) {
                kotlin.time.Instant.fromEpochMilliseconds(earliestTransactionMs)
                    .minus(1, DateTimeUnit.YEAR, TimeZone.UTC)
                    .toEpochMilliseconds()
            } else {
                maxOf(ranges[0], earliestTransactionMs)
            }
            val effectiveToMs = if (isAllTime) {
                kotlin.time.Instant.fromEpochMilliseconds(latestTransactionMs)
                    .plus(1, DateTimeUnit.YEAR, TimeZone.UTC)
                    .toEpochMilliseconds()
            } else {
                ranges[1]
            }

            var fromDate = kotlin.time.Instant.fromEpochMilliseconds(effectiveFromMs)
            val toDate = kotlin.time.Instant.fromEpochMilliseconds(effectiveToMs)

            val dates = ArrayList<String>()
            val expenses = ArrayList<FloatEntryModel>()
            val incomes = ArrayList<FloatEntryModel>()
            var index = 0

            // MAX_CHART_POINTS is a safety net for wide CUSTOM ranges (e.g. a custom daily
            // range spanning several years). For DateRangeType.ALL this cap is never hit
            // because the loop is bounded by actual transaction years above.
            while (fromDate <= toDate && index < MAX_CHART_POINTS) {
                val key = groupValue(groupType, fromDate.toEpochMilliseconds().toCompleteDate())
                val totals = bucketTotals[key]
                val totalExpense = totals?.get(0) ?: 0.0
                val totalIncome = totals?.get(1) ?: 0.0

                dates.add(key)
                expenses.add(
                    FloatEntryModel(
                        index, totalExpense,
                        if (totalExpense == 0.0) zeroFormatted
                        else getFormattedAmountUseCase.invoke(totalExpense, currency)
                    )
                )
                incomes.add(
                    FloatEntryModel(
                        index, totalIncome,
                        if (totalIncome == 0.0) zeroFormatted
                        else getFormattedAmountUseCase.invoke(totalIncome, currency)
                    )
                )

                fromDate = getAdjustedDateTime(groupType, fromDate)
                index++
            }

            AnalysisData(
                transactionItems,
                if (bucketTotals.isEmpty()) null
                else AnalysisChartData(listOf(expenses, incomes), dates)
            )
        }.flowOn(dispatcher.computation)
    }

    @OptIn(ExperimentalTime::class)
    private fun getAdjustedDateTime(
        groupType: GroupType,
        fromDate: kotlin.time.Instant,
    ) = when (groupType) {
        GroupType.YEAR -> fromDate.plus(1, DateTimeUnit.YEAR, TimeZone.UTC)
        GroupType.MONTH -> fromDate.plus(1, DateTimeUnit.MONTH, TimeZone.UTC)
        GroupType.DATE -> fromDate.plus(1, DateTimeUnit.DAY, TimeZone.UTC)
    }

    private fun groupValue(groupType: GroupType, transactionDate: Date) = when (groupType) {
        GroupType.YEAR -> transactionDate.toYear()
        GroupType.MONTH -> transactionDate.toMonthAndYear()
        GroupType.DATE -> transactionDate.toCompleteDateWithDate()
    }

    companion object {
        // Caps the chart series length. A daily chart over 500 days is already hard to
        // read on mobile; this also prevents the while loop from allocating unboundedly
        // when the date range is very wide (e.g. DateRangeType.ALL).
        private const val MAX_CHART_POINTS = 500
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
    val chartData: List<List<FloatEntryModel>>,
    val dates: List<String>,
    val title: String? = null,
)
