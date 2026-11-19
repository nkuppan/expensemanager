package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertedTotal
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetExpenseAmountUseCase(
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val convertAmountUseCase: ConvertAmountUseCase,
) {

    operator fun invoke(): Flow<ConvertedTotal> = combine(
        getTransactionWithFilterUseCase.invoke(),
        getCurrencyUseCase.invoke(),
    ) { transactions, currency ->
        convertAmountUseCase.total(
            amounts = transactions.orEmpty()
                .filter { it.type == TransactionType.EXPENSE }
                .map { it.amount.amount to it.fromCurrencyCode },
            toCode = currency.code,
        )
    }
}
