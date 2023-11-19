package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.data.repository.BudgetRepository
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetBudgetDetailUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
) {
    suspend operator fun invoke(budgetId: String?): Resource<BudgetUiModel> {

        if (budgetId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid budget id value"))
        }

        return when (val response = repository.findBudgetById(budgetId)) {
            is Resource.Error -> response
            is Resource.Success -> {
                val budget = response.data
                val currency = getCurrencyUseCase.invoke().first()
                val transactions = when (val transaction = getBudgetTransactionsUseCase(budget)) {
                    is Resource.Error -> {
                        null
                    }

                    is Resource.Success -> {
                        transaction.data.filter {
                            it.type.isExpense()
                        }
                    }
                }
                val transactionAmount = transactions?.sumOf { it.amount.amount } ?: 0.0
                val percent = (transactionAmount / budget.amount).toFloat() * 100
                return Resource.Success(
                    budget.toBudgetUiModel(
                        budgetAmount = getFormattedAmountUseCase(budget.amount, currency),
                        transactionAmount = getFormattedAmountUseCase(transactionAmount, currency),
                        percent,
                        transactions?.map {
                            it.toTransactionUIModel(
                                getFormattedAmountUseCase(it.amount.amount, currency)
                            )
                        }
                    )
                )
            }
        }
    }
}