package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import javax.inject.Inject

class FindBudgetByIdUseCase @Inject constructor(
    private val repository: BudgetRepository,
) {

    suspend operator fun invoke(budgetId: String?): Resource<Budget> {
        if (budgetId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid budget id value"))
        }

        return repository.findBudgetById(budgetId)
    }
}
