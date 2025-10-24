package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class GetBudgetDetailUseCase(
    private val budgetRepository: BudgetRepository,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
) {
    operator fun invoke(budgetId: String): Flow<BudgetUiModel?> {
        return combine(
            budgetRepository.findBudgetByIdFlow(budgetId),
            getTransactionWithFilterUseCase.invoke(),
        ) { budget, _ ->
            budget?.let {
                val currency = getCurrencyUseCase.invoke().first()
                val budgetTransactions =
                    when (val transaction = getBudgetTransactionsUseCase.invoke(budget)) {
                        is Resource.Error -> {
                            null
                        }

                        is Resource.Success -> {
                            transaction.data.filter {
                                it.type.isExpense()
                            }
                        }
                    }
                val transactionAmount = budgetTransactions?.sumOf { it.amount.amount } ?: 0.0
                val percent = (transactionAmount / budget.amount).toFloat() * 100
                budget.toBudgetUiModel(
                    budgetAmount = getFormattedAmountUseCase(budget.amount, currency),
                    transactionAmount = getFormattedAmountUseCase(transactionAmount, currency),
                    percent,
                    budgetTransactions?.map {
                        it.toTransactionUIModel(
                            getFormattedAmountUseCase(it.amount.amount, currency),
                        )
                    },
                )
            }
        }
    }
}
