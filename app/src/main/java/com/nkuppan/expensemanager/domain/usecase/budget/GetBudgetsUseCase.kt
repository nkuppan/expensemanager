package com.nkuppan.expensemanager.domain.usecase.budget

import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Currency
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.isExpense
import com.nkuppan.expensemanager.domain.repository.BudgetRepository
import com.nkuppan.expensemanager.domain.usecase.settings.currency.GetCurrencyUseCase
import com.nkuppan.expensemanager.ui.utils.UiText
import com.nkuppan.expensemanager.ui.utils.getCurrency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
) {
    operator fun invoke(): Flow<List<BudgetUiModel>> {
        return combine(
            getCurrencyUseCase.invoke(),
            repository.getBudgets()
        ) { currency, budgets ->
            budgets.map {
                val transactionAmount =
                    when (val response = getBudgetTransactionsUseCase(it)) {
                        is Resource.Error -> {
                            0.0
                        }

                        is Resource.Success -> {
                            response.data.filter { it.type.isExpense() }.sumOf { it.amount }
                        }
                    }
                val percent = (transactionAmount / it.amount).toFloat() * 100
                it.toBudgetUiModel(
                    currency,
                    transactionAmount,
                    when {
                        percent < 0f -> R.color.green_500
                        percent in 0f..35f -> R.color.green_500
                        percent in 36f..60f -> R.color.light_green_500
                        percent in 61f..85f -> R.color.orange_500
                        else -> R.color.red_500
                    },
                    percent
                )
            }
        }
    }
}


fun Budget.toBudgetUiModel(
    currency: Currency,
    transactionAmount: Double,
    progressBarColor: Int,
    percent: Float,
) = BudgetUiModel(
    id = this.id,
    name = this.name,
    icon = this.iconName,
    iconBackgroundColor = this.iconBackgroundColor,
    progressBarColor = progressBarColor,
    amount = getCurrency(
        currency,
        this.amount
    ),
    transactionAmount = getCurrency(
        currency,
        transactionAmount
    ),
    percent = percent
)


data class BudgetUiModel(
    val id: String,
    val name: String,
    val icon: String,
    val iconBackgroundColor: String,
    val progressBarColor: Int,
    val amount: UiText,
    val transactionAmount: UiText,
    val percent: Float,
)