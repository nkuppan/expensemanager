package com.nkuppan.expensemanager.domain.usecase.budget

import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.BudgetRepository
import javax.inject.Inject

class AddBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val checkBudgetValidateUseCase: CheckBudgetValidateUseCase
) {

    suspend operator fun invoke(budget: Budget): Resource<Boolean> {
        return when (val validationResult = checkBudgetValidateUseCase(budget)) {
            is Resource.Error -> {
                validationResult
            }

            is Resource.Success -> {
                repository.addBudget(budget)
            }
        }
    }
}