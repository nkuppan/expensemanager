package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BudgetRepository

class UpdateBudgetUseCase(
    private val repository: BudgetRepository,
    private val checkBudgetValidateUseCase: CheckBudgetValidateUseCase,
) {

    suspend operator fun invoke(budget: Budget): Resource<Boolean> {
        val validationResult = checkBudgetValidateUseCase(budget)
        if (validationResult is Resource.Error) return validationResult

        // The limit is defined in the budget's currency: that can never change, or the
        // same number would silently mean a different amount of money.
        val existing = repository.findBudgetById(budget.id)
        if (existing is Resource.Success && existing.data.currencyCode != budget.currencyCode) {
            return Resource.Error(Exception("A budget's currency can't be changed"))
        }
        return repository.updateBudget(budget)
    }
}
