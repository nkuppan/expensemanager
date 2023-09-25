package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun getBudgets(): Flow<List<Budget>>

    suspend fun findBudget(budgetId: String): Resource<Budget>

    suspend fun addBudget(budget: Budget): Resource<Boolean>

    suspend fun updateBudget(budget: Budget): Resource<Boolean>

    suspend fun deleteBudget(budget: Budget): Resource<Boolean>
}