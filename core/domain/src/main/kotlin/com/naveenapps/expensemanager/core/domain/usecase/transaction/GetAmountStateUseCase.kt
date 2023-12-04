package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.AmountUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetAmountStateUseCase @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getIncomeAmountUseCase: GetIncomeAmountUseCase,
    private val getExpenseAmountUseCase: GetExpenseAmountUseCase,
    private val dispatcher: AppCoroutineDispatchers,
) {
    fun invoke(): Flow<AmountUiState> {
        return combine(
            getCurrencyUseCase.invoke(),
            getIncomeAmountUseCase.invoke(),
            getExpenseAmountUseCase.invoke(),
        ) { currency, income, expense ->

            val incomeValue = income ?: 0.0
            val expenseValue = expense ?: 0.0
            AmountUiState(
                income = getFormattedAmountUseCase.invoke(
                    incomeValue,
                    currency,
                ).amountString.orEmpty(),
                expense = getFormattedAmountUseCase.invoke(
                    expenseValue,
                    currency,
                ).amountString.orEmpty(),
                balance = getFormattedAmountUseCase.invoke(
                    (incomeValue - expenseValue),
                    currency,
                ).amountString.orEmpty(),
            )
        }.flowOn(dispatcher.computation)
    }
}
