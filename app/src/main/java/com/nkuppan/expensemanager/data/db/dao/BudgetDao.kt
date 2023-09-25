package com.nkuppan.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nkuppan.expensemanager.data.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface BudgetDao : BaseDao<BudgetEntity> {

    @Query("SELECT * FROM budget ORDER BY created_on DESC")
    fun getBudgets(): Flow<List<BudgetEntity>?>

    @Query("SELECT * FROM budget WHERE id = :id")
    fun findById(id: String): BudgetEntity?

    @Query("SELECT COUNT(*) FROM budget")
    fun getCount(): Long
}