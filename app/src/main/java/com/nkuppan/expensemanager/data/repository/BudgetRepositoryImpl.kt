package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.data.db.dao.BudgetDao
import com.nkuppan.expensemanager.data.mappers.toDomainModel
import com.nkuppan.expensemanager.data.mappers.toEntityModel
import com.nkuppan.expensemanager.domain.model.Budget
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val dispatchers: AppCoroutineDispatchers
) : BudgetRepository {

    override fun getBudgets(): Flow<List<Budget>> {
        return budgetDao.getBudgets().map { budgets ->
            return@map budgets?.map { it.toDomainModel() } ?: emptyList()
        }
    }

    override suspend fun findBudget(budgetId: String): Resource<Budget> =
        withContext(dispatchers.io) {
            return@withContext try {
                val budget = budgetDao.findById(budgetId)

                if (budget != null) {
                    Resource.Success(budget.toDomainModel())
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addBudget(budget: Budget): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = budgetDao.insert(budget.toEntityModel())
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateBudget(budget: Budget): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                budgetDao.update(budget.toEntityModel())
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteBudget(budget: Budget): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = budgetDao.delete(budget.toEntityModel())
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}