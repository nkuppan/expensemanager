package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.FormatAmountInCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currencyapi.ConvertedTotal
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.toTransactionUIModel

/**
 * A budget is evaluated in its own, fixed [Budget.currencyCode]: the limit is in that currency,
 * and every linked expense is converted into it under the user's exchange method. The app's
 * display currency plays no part, so changing it never changes what a budget's limit means. If
 * any expense can't be converted, spending and progress are reported unavailable instead of a
 * partial sum; with conversion disabled, only expenses in the budget's own currency count.
 */
class CalculateBudgetUseCase(
    private val getBudgetTransactionsUseCase: GetBudgetTransactionsUseCase,
    private val convertAmountUseCase: ConvertAmountUseCase,
    private val formatAmountInCurrencyUseCase: FormatAmountInCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
) {
    suspend operator fun invoke(budget: Budget): BudgetUiModel {
        val expenses = when (val response = getBudgetTransactionsUseCase(budget)) {
            is Resource.Error -> null
            is Resource.Success -> response.data.filter { it.type.isExpense() }
        }
        val budgetCurrency = formatAmountInCurrencyUseCase.currencyFor(budget.currencyCode)
        val amounts = expenses.orEmpty().map { it.amount.amount to it.fromCurrencyCode }
        val converted = convertAmountUseCase.total(amounts, budget.currencyCode)
        // With conversion disabled only spending in the budget's own currency counts; the rest
        // is left out (and flagged) rather than converted or added in as if it were the same.
        val otherCurrenciesExcluded = converted.conversionDisabled &&
            amounts.any { it.second != budget.currencyCode }
        val spent = if (converted.conversionDisabled) {
            ConvertedTotal(
                value = amounts.filter { it.second == budget.currencyCode }.sumOf { it.first },
            )
        } else {
            converted
        }
        val spentValue = spent.value
        val percent = if (spentValue != null && budget.amount > 0.0) {
            (spentValue / budget.amount).toFloat() * 100
        } else {
            0f
        }
        return budget.toBudgetUiModel(
            title = budget.title(),
            budgetAmount = getFormattedAmountUseCase(budget.amount, budgetCurrency),
            transactionAmount = getFormattedAmountUseCase(spent, budgetCurrency),
            percent = percent,
            transactions = expenses?.map {
                it.toTransactionUIModel(
                    formatAmountInCurrencyUseCase(it.amount.amount, it.fromCurrencyCode),
                )
            },
        ).copy(
            convertedAtCurrentRates = spent.usedConversion,
            otherCurrenciesExcluded = otherCurrenciesExcluded,
        )
    }
}
