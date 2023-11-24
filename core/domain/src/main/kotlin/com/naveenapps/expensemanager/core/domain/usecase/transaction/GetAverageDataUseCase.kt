package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.WholeAverageData
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import javax.inject.Inject

class GetAverageDataUseCase @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val dispatcher: AppCoroutineDispatchers,
) {
    fun invoke(): Flow<WholeAverageData> {
        return combine(
            getCurrencyUseCase.invoke(),
            getDateRangeUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
        ) { currency, dateRangeModel, transactions ->
            val incomeAmount =
                transactions?.filter { it.type.isIncome() }?.sumOf { it.amount.amount } ?: 0.0
            val expenseAmount =
                transactions?.filter { it.type.isExpense() }?.sumOf { it.amount.amount } ?: 0.0

            val ranges = dateRangeModel.dateRanges

            val calendar: Calendar = Calendar.getInstance()
            calendar.timeInMillis = ranges[0]

            val weeksMultiplier: Double
            val daysMultiplier: Double
            val monthMultiplier: Double


            when (dateRangeModel.type) {
                DateRangeType.TODAY -> {
                    daysMultiplier = 1.0
                    weeksMultiplier = calendar.getActualMaximum(Calendar.DAY_OF_WEEK).toDouble()
                    monthMultiplier = calendar.getActualMaximum(Calendar.DAY_OF_MONTH).toDouble()
                }

                DateRangeType.THIS_WEEK -> {
                    daysMultiplier = 1.0 / calendar.getActualMaximum(Calendar.DAY_OF_WEEK)
                    weeksMultiplier = 1.0
                    monthMultiplier = calendar.getActualMaximum(Calendar.WEEK_OF_MONTH).toDouble()
                }

                DateRangeType.THIS_MONTH -> {
                    daysMultiplier = 1.0 / calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    weeksMultiplier = 1.0  / calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)
                    monthMultiplier = 1.0
                }

                DateRangeType.THIS_YEAR,
                DateRangeType.CUSTOM,
                DateRangeType.ALL -> {
                    daysMultiplier = 1.0 / calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                    weeksMultiplier = 1.0  / calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
                    monthMultiplier = 1.0 / 12
                }
            }

            WholeAverageData(
                expenseAverageData = AverageData(
                    perDay = getFormattedAmountUseCase.invoke(
                        (expenseAmount * daysMultiplier),
                        currency
                    ).amountString.orEmpty(),
                    perWeek = getFormattedAmountUseCase.invoke(
                        (expenseAmount * weeksMultiplier),
                        currency
                    ).amountString.orEmpty(),
                    perMonth = getFormattedAmountUseCase.invoke(
                        (expenseAmount * monthMultiplier),
                        currency
                    ).amountString.orEmpty(),
                ),
                incomeAverageData = AverageData(
                    perDay = getFormattedAmountUseCase.invoke(
                        (incomeAmount * daysMultiplier),
                        currency
                    ).amountString.orEmpty(),
                    perWeek = getFormattedAmountUseCase.invoke(
                        (incomeAmount * weeksMultiplier),
                        currency
                    ).amountString.orEmpty(),
                    perMonth = getFormattedAmountUseCase.invoke(
                        (incomeAmount * monthMultiplier),
                        currency
                    ).amountString.orEmpty(),
                )
            )
        }.flowOn(dispatcher.computation)
    }
}