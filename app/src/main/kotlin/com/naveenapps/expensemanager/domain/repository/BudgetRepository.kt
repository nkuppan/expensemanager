package com.naveenapps.expensemanager.domain.repository

import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun getBudgets(): Flow<List<Budget>>

    suspend fun findBudgetById(budgetId: String): Resource<Budget>

    suspend fun addBudget(budget: Budget): Resource<Boolean>

    suspend fun updateBudget(budget: Budget): Resource<Boolean>

    suspend fun deleteBudget(budget: Budget): Resource<Boolean>
}