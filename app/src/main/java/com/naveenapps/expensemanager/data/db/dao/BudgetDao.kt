package com.naveenapps.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naveenapps.expensemanager.data.db.entity.BudgetAccountEntity
import com.naveenapps.expensemanager.data.db.entity.BudgetCategoryEntity
import com.naveenapps.expensemanager.data.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID


@Dao
interface BudgetDao : BaseDao<BudgetEntity> {

    @Query("SELECT * FROM budget ORDER BY created_on DESC")
    fun getBudgets(): Flow<List<BudgetEntity>?>

    @Query("SELECT * FROM budget WHERE id = :id")
    suspend fun findById(id: String): BudgetEntity?

    @Query("SELECT * FROM budget_category_relation WHERE budget_id=:budgetId ORDER BY created_on DESC")
    suspend fun getBudgetCategories(budgetId: String): List<BudgetCategoryEntity>?

    @Query("SELECT * FROM budget_account_relation WHERE budget_id=:budgetId ORDER BY created_on DESC")
    suspend fun getBudgetAccounts(budgetId: String): List<BudgetAccountEntity>?

    @Query("DELETE FROM budget_category_relation WHERE budget_id = :budgetId")
    suspend fun removeBudgetCategories(budgetId: String)

    @Query("DELETE FROM budget_account_relation WHERE budget_id = :budgetId")
    suspend fun removeBudgetAccounts(budgetId: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudget(budgetEntity: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudgetAccountRelation(budgetAccountEntity: BudgetAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudgetCategoryRelation(budgetCategoryEntity: BudgetCategoryEntity): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateBudget(budgetEntity: BudgetEntity)

    @Transaction
    suspend fun insertBudget(
        budgetEntity: BudgetEntity,
        categories: List<String>,
        accounts: List<String>
    ): Long {
        val id = insertBudget(budgetEntity)
        if (id != -1L) {
            if (categories.isNotEmpty()) {
                categories.forEach { categoryId ->
                    insertBudgetCategoryRelation(
                        BudgetCategoryEntity(
                            id = UUID.randomUUID().toString(),
                            budgetId = budgetEntity.id,
                            categoryId = categoryId,
                            createdOn = Date(),
                            updatedOn = Date()
                        )
                    )
                }
            }
            if (accounts.isNotEmpty()) {
                accounts.forEach { accountId ->
                    insertBudgetAccountRelation(
                        BudgetAccountEntity(
                            id = UUID.randomUUID().toString(),
                            budgetId = budgetEntity.id,
                            accountId = accountId,
                            createdOn = Date(),
                            updatedOn = Date()
                        )
                    )
                }
            }
        }
        return id
    }

    @Transaction
    suspend fun updateBudget(
        budgetEntity: BudgetEntity,
        categories: List<String>,
        accounts: List<String>
    ) {
        updateBudget(budgetEntity)

        removeBudgetCategories(budgetId = budgetEntity.id)
        removeBudgetAccounts(budgetId = budgetEntity.id)

        if (categories.isNotEmpty()) {
            categories.forEach { categoryId ->
                insertBudgetCategoryRelation(
                    BudgetCategoryEntity(
                        id = UUID.randomUUID().toString(),
                        budgetId = budgetEntity.id,
                        categoryId = categoryId,
                        createdOn = Date(),
                        updatedOn = Date()
                    )
                )
            }
        }
        if (accounts.isNotEmpty()) {
            accounts.forEach { accountId ->
                insertBudgetAccountRelation(
                    BudgetAccountEntity(
                        id = UUID.randomUUID().toString(),
                        budgetId = budgetEntity.id,
                        accountId = accountId,
                        createdOn = Date(),
                        updatedOn = Date()
                    )
                )
            }
        }
    }
}