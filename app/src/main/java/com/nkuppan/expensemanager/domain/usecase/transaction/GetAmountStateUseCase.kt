package com.nkuppan.expensemanager.domain.usecase.transaction

import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.presentation.dashboard.AmountUiState
import com.nkuppan.expensemanager.ui.utils.getBalanceCurrency
import com.nkuppan.expensemanager.ui.utils.getCurrency
import com.nkuppan.expensemanager.utils.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetAmountStateUseCase @Inject constructor(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getIncomeAmountUseCase: GetIncomeAmountUseCase,
    private val getExpenseAmountUseCase: GetExpenseAmountUseCase,
    private val dispatcher: AppCoroutineDispatchers,
) {
    fun invoke(): Flow<AmountUiState> {
        return combine(
            getCurrencyUseCase.invoke(),
            getIncomeAmountUseCase.invoke(),
            getExpenseAmountUseCase.invoke()
        ) { currency, income, expense ->

            val incomeValue = income ?: 0.0
            val expenseValue = expense ?: 0.0
            val incomeAmount = getCurrency(currency, incomeValue)
            val expenseAmount = getCurrency(currency, expenseValue)
            val balanceAmount = getBalanceCurrency(currency, (incomeValue - expenseValue))

            AmountUiState(
                income = incomeAmount,
                expense = expenseAmount,
                balance = balanceAmount,
            )
        }.flowOn(dispatcher.computation)
    }
}