package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<CategoryEntity> {

    @Query("SELECT * FROM category ORDER BY created_on DESC")
    fun getCategories(): Flow<List<CategoryEntity>?>

    @Query("SELECT * FROM category WHERE id = :aModelId")
    fun findByIdFlow(aModelId: String): Flow<CategoryEntity?>

    @Query("SELECT * FROM category ORDER BY created_on DESC")
    suspend fun getAllValues(): List<CategoryEntity>?

    @Query("SELECT * FROM category WHERE id = :aModelId")
    suspend fun findById(aModelId: String): CategoryEntity?
}
