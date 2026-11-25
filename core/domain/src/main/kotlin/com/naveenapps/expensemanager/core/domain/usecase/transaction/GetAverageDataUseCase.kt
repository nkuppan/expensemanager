package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.WholeAverageData
import com.naveenapps.expensemanager.core.model.netExpense
import com.naveenapps.expensemanager.core.model.netIncome
import com.naveenapps.expensemanager.core.repository.CalendarWeeksRepository
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class GetAverageDataUseCase(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val calendarWeeksRepository: CalendarWeeksRepository,
    private val dispatcher: AppCoroutineDispatchers,
) {
    fun invoke(): Flow<WholeAverageData> = combine(
        getCurrencyUseCase.invoke(),
        getDateRangeUseCase.invoke(),
        getTransactionWithFilterUseCase.invoke(),
    ) { currency, dateRangeModel, transactions ->
        val incomeAmount = transactions?.netIncome() ?: 0.0
        val expenseAmount = transactions?.netExpense() ?: 0.0

        val ranges = dateRangeModel.dateRanges

        val (daysMultiplier, weeksMultiplier, monthMultiplier) = averageMultipliers(
            type = dateRangeModel.type,
            rangeStart = Instant.fromEpochMilliseconds(ranges[0]),
            weeks = calendarWeeksRepository,
        )

        WholeAverageData(
            expenseAverageData = AverageData(
                perDay = getFormattedAmountUseCase.invoke(
                    (expenseAmount * daysMultiplier),
                    currency,
                ).amountString.orEmpty(),
                perWeek = getFormattedAmountUseCase.invoke(
                    (expenseAmount * weeksMultiplier),
                    currency,
                ).amountString.orEmpty(),
                perMonth = getFormattedAmountUseCase.invoke(
                    (expenseAmount * monthMultiplier),
                    currency,
                ).amountString.orEmpty(),
            ),
            incomeAverageData = AverageData(
                perDay = getFormattedAmountUseCase.invoke(
                    (incomeAmount * daysMultiplier),
                    currency,
                ).amountString.orEmpty(),
                perWeek = getFormattedAmountUseCase.invoke(
                    (incomeAmount * weeksMultiplier),
                    currency,
                ).amountString.orEmpty(),
                perMonth = getFormattedAmountUseCase.invoke(
                    (incomeAmount * monthMultiplier),
                    currency,
                ).amountString.orEmpty(),
            ),
        )
    }.flowOn(dispatcher.computation)
}

/** Per-day, per-week and per-month multipliers for a range of [type] starting at [rangeStart]. */
internal data class AverageMultipliers(val perDay: Double, val perWeek: Double, val perMonth: Double)

internal fun averageMultipliers(
    type: DateRangeType,
    rangeStart: Instant,
    weeks: CalendarWeeksRepository,
    zone: TimeZone = TimeZone.currentSystemDefault(),
): AverageMultipliers {
    val date = rangeStart.toLocalDateTime(zone).date
    val firstOfMonth = LocalDate(date.year, date.month, 1)
    val daysInMonth = firstOfMonth.daysUntil(firstOfMonth.plus(1, DateTimeUnit.MONTH))
    val daysInYear = LocalDate(date.year, 1, 1).daysUntil(LocalDate(date.year + 1, 1, 1))
    return when (type) {
        DateRangeType.TODAY -> AverageMultipliers(1.0, DAYS_PER_WEEK.toDouble(), daysInMonth.toDouble())

        DateRangeType.THIS_WEEK -> AverageMultipliers(1.0 / DAYS_PER_WEEK, 1.0, weeks.weeksInMonthOf(rangeStart).toDouble())

        DateRangeType.THIS_MONTH -> AverageMultipliers(1.0 / daysInMonth, 1.0 / weeks.weeksInMonthOf(rangeStart), 1.0)

        DateRangeType.THIS_YEAR,
        DateRangeType.CUSTOM,
        DateRangeType.ALL,
        -> AverageMultipliers(1.0 / daysInYear, 1.0 / weeks.weeksInYearOf(rangeStart), 1.0 / 12)
    }
}

private const val DAYS_PER_WEEK = 7
