package com.nkuppan.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nkuppan.expensemanager.data.db.entity.CategoryEntity


@Dao
interface CategoryDao : BaseDao<CategoryEntity> {

    @Query("SELECT * FROM category ORDER BY is_favorite DESC")
    fun getAllValues(): List<CategoryEntity>?

    @Query("SELECT * FROM category WHERE id = :aModelId")
    fun findById(aModelId: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM category")
    fun getCount(): Long
}