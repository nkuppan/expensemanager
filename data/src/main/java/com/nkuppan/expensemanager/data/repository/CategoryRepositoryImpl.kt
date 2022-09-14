package com.nkuppan.expensemanager.data.repository

import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.db.dao.CategoryDao
import com.nkuppan.expensemanager.data.mappers.CategoryDomainEntityMapper
import com.nkuppan.expensemanager.data.mappers.CategoryEntityDomainMapper
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val categoryDomainEntityMapper: CategoryDomainEntityMapper,
    private val categoryEntityDomainMapper: CategoryEntityDomainMapper,
    private val dispatchers: AppCoroutineDispatchers
) : CategoryRepository {

    override suspend fun getAllCategory(): Resource<List<Category>> = withContext(dispatchers.io) {
        return@withContext try {
            val category = categoryDao.getAllValues()

            if (category != null) {
                Resource.Success(category.map { categoryEntityDomainMapper.convert(it) })
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
                    Resource.Success(categoryEntityDomainMapper.convert(category))
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
                val response = categoryDao.insert(categoryDomainEntityMapper.convert(category))
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateCategory(category: Category): Resource<Boolean>  =
        withContext(dispatchers.io) {
            return@withContext try {
                categoryDao.update(categoryDomainEntityMapper.convert(category))
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteCategory(category: Category): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = categoryDao.delete(categoryDomainEntityMapper.convert(category))
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}