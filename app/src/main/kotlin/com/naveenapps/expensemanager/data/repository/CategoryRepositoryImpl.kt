package com.naveenapps.expensemanager.data.repository

import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.data.mappers.toDomainModel
import com.naveenapps.expensemanager.data.mappers.toEntityModel
import com.naveenapps.expensemanager.domain.repository.CategoryRepository
import com.naveenapps.expensemanager.utils.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val dispatchers: AppCoroutineDispatchers
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getCategories().map { categories ->
            return@map categories?.map { it.toDomainModel() } ?: emptyList()
        }
    }

    override suspend fun getAllCategory(): Resource<List<Category>> = withContext(dispatchers.io) {
        return@withContext try {
            val category = categoryDao.getAllValues()

            if (category != null) {
                Resource.Success(category.map { it.toDomainModel() })
            } else {
                Resource.Error(KotlinNullPointerException())
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun findCategory(categoryId: String): Resource<Category> =
        withContext(dispatchers.io) {
            return@withContext try {
                val category = categoryDao.findById(categoryId)

                if (category != null) {
                    Resource.Success(category.toDomainModel())
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addCategory(category: Category): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = categoryDao.insert(category.toEntityModel())
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateCategory(category: Category): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                categoryDao.update(category.toEntityModel())
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteCategory(category: Category): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = categoryDao.delete(category.toEntityModel())
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}