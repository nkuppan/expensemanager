package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.common.R
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
) {
    operator fun invoke(): Flow<List<BudgetUiModel>> {
        return combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
            repository.getBudgets(),
        ) { currency, _, budgets ->
            budgets.map {
                val transactions = when (val response = getBudgetTransactionsUseCase(it)) {
                    is Resource.Error -> {
                        null
                    }

                    is Resource.Success -> {
                        response.data.filter {
                            it.type.isExpense()
                        }
                    }
                }
                val transactionAmount = transactions?.sumOf { it.amount.amount } ?: 0.0
                val percent = (transactionAmount / it.amount).toFloat() * 100
                it.toBudgetUiModel(
                    budgetAmount = getFormattedAmountUseCase(it.amount, currency),
                    transactionAmount = getFormattedAmountUseCase(transactionAmount, currency),
                    percent,
                    transactions?.map {
                        it.toTransactionUIModel(
                            getFormattedAmountUseCase(it.amount.amount, currency),
                        )
                    },
                )
            }
        }
    }
}

fun Budget.toBudgetUiModel(
    budgetAmount: Amount,
    transactionAmount: Amount,
    percent: Float,
    transactions: List<TransactionUiItem>? = null,
) = BudgetUiModel(
    id = this.id,
    name = this.name,
    icon = this.storedIcon.name,
    iconBackgroundColor = this.storedIcon.backgroundColor,
    progressBarColor = when {
        percent < 0f -> R.color.green_500
        percent in 0f..35f -> R.color.green_500
        percent in 36f..60f -> R.color.light_green_500
        percent in 61f..85f -> R.color.orange_500
        else -> R.color.red_500
    },
    amount = budgetAmount,
    transactionAmount = transactionAmount,
    percent = percent,
    transactions = transactions,
)

data class BudgetUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val iconBackgroundColor: String,
    val progressBarColor: Int,
    val amount: Amount,
    val transactionAmount: Amount,
    val percent: Float,
    val transactions: List<TransactionUiItem>? = null,
)
