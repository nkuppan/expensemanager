package com.nkuppan.expensemanager.domain.usecase.budget

import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.BudgetRepository
import javax.inject.Inject

class FindBudgetByIdUseCase @Inject constructor(
    private val repository: BudgetRepository
) {

    suspend operator fun invoke(budgetId: String?): Resource<Budget> {

        if (budgetId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid budget id value"))
        }

        return repository.findBudgetById(budgetId)
    }
}