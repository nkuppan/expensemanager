package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import javax.inject.Inject

class DeleteBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val checkBudgetValidateUseCase: CheckBudgetValidateUseCase,
) {

    suspend operator fun invoke(budget: Budget): Resource<Boolean> {
        return when (val validationResult = checkBudgetValidateUseCase(budget)) {
            is Resource.Error -> {
                validationResult
            }

            is Resource.Success -> {
                repository.deleteBudget(budget)
            }
        }
    }
}
