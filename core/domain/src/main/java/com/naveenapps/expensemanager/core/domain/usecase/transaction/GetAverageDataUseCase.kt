package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.daterange.GetDateRangeUseCase
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

            val weeksPerMonth = when (dateRangeModel.type) {
                DateRangeType.TODAY,
                DateRangeType.THIS_WEEK,
                DateRangeType.THIS_MONTH -> {
                    1 / calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)
                }

                DateRangeType.CUSTOM,
                DateRangeType.ALL,
                DateRangeType.THIS_YEAR -> {
                    1 / calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
                }
            }

            val daysPerMonth = when (dateRangeModel.type) {
                DateRangeType.TODAY,
                DateRangeType.THIS_WEEK,
                DateRangeType.THIS_MONTH -> {
                    1 / calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                }

                DateRangeType.CUSTOM,
                DateRangeType.ALL,
                DateRangeType.THIS_YEAR -> {
                    1 / calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                }
            }

            val monthsPerYear = when (dateRangeModel.type) {
                DateRangeType.TODAY,
                DateRangeType.THIS_WEEK,
                DateRangeType.THIS_MONTH -> {
                    1 / 1
                }

                DateRangeType.CUSTOM,
                DateRangeType.ALL,
                DateRangeType.THIS_YEAR -> {
                    1 / calendar.getActualMaximum(Calendar.MONTH) + 1
                }
            }

            WholeAverageData(
                expenseAverageData = AverageData(
                    perDay = getFormattedAmountUseCase.invoke(
                        (expenseAmount * daysPerMonth),
                        currency
                    ).amountString.orEmpty(),
                    perWeek = getFormattedAmountUseCase.invoke(
                        (expenseAmount * weeksPerMonth),
                        currency
                    ).amountString.orEmpty(),
                    perMonth = getFormattedAmountUseCase.invoke(
                        (expenseAmount * monthsPerYear),
                        currency
                    ).amountString.orEmpty(),
                ),
                incomeAverageData = AverageData(
                    perDay = getFormattedAmountUseCase.invoke(
                        (incomeAmount * daysPerMonth),
                        currency
                    ).amountString.orEmpty(),
                    perWeek = getFormattedAmountUseCase.invoke(
                        (incomeAmount * weeksPerMonth),
                        currency
                    ).amountString.orEmpty(),
                    perMonth = getFormattedAmountUseCase.invoke(
                        (incomeAmount * monthsPerYear),
                        currency
                    ).amountString.orEmpty(),
                )
            )
        }.flowOn(dispatcher.computation)
    }
}