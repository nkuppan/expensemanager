package com.nkuppan.expensemanager.domain.usecase.budget

import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(private val repository: BudgetRepository) {
    operator fun invoke(): Flow<List<Budget>> {
        return repository.getBudgets()
    }
}