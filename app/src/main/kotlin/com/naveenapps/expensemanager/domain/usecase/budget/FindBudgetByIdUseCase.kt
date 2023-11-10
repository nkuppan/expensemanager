package com.naveenapps.expensemanager.domain.usecase.budget

import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class FindBudgetByIdUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.BudgetRepository
) {

    suspend operator fun invoke(budgetId: String?): Resource<Budget> {

        if (budgetId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid budget id value"))
        }

        return repository.findBudgetById(budgetId)
    }
}