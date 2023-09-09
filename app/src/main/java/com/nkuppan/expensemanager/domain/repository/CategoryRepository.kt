package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Category related api for database operations
 */
interface CategoryRepository {

    fun getCategories(): Flow<List<Category>>

    suspend fun getAllCategory(): Resource<List<Category>>

    suspend fun findCategory(categoryId: String): Resource<Category>

    suspend fun addCategory(category: Category): Resource<Boolean>

    suspend fun updateCategory(category: Category): Resource<Boolean>

    suspend fun deleteCategory(category: Category): Resource<Boolean>
}