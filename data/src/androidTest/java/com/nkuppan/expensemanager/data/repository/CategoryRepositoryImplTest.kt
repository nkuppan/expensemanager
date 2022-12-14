package com.nkuppan.expensemanager.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nkuppan.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.testing.BaseCoroutineTest
import com.nkuppan.expensemanager.data.db.ExpenseManagerDatabase
import com.nkuppan.expensemanager.data.db.dao.CategoryDao
import com.nkuppan.expensemanager.data.mappers.CategoryDomainEntityMapper
import com.nkuppan.expensemanager.data.mappers.CategoryEntityDomainMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CategoryRepositoryImplTest : BaseCoroutineTest() {

    private lateinit var categoryDao: CategoryDao

    private lateinit var database: ExpenseManagerDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var categoryRepository: CategoryRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseManagerDatabase::class.java
        ).allowMainThreadQueries().build()

        categoryDao = database.categoryDao()

        categoryRepository = CategoryRepositoryImpl(
            categoryDao,
            CategoryDomainEntityMapper(),
            CategoryEntityDomainMapper(),
            AppCoroutineDispatchers(
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        database.close()
    }

    @Test
    fun checkDatabaseObject() {
        Truth.assertThat(database).isNotNull()
        Truth.assertThat(categoryDao).isNotNull()
    }

    @Test
    fun checkInsertSuccessCase() = runTest {
        val result = categoryRepository.addCategory(FAKE_CATEGORY)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    @Test
    fun checkDeleteSuccessCase() = runTest {
        var result = categoryRepository.addCategory(FAKE_CATEGORY)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        var data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        result = categoryRepository.deleteCategory(FAKE_CATEGORY)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    @Test
    fun checkFindByIdSuccessCase() = runTest {
        val result = categoryRepository.addCategory(FAKE_CATEGORY)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        val newResult = categoryRepository.findCategory(FAKE_CATEGORY.id)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(FAKE_CATEGORY.id)
    }

    @Test
    fun checkGetAllCategoriesSuccessCase() = runTest {
        val result = categoryRepository.addCategory(FAKE_CATEGORY)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        val newResult = categoryRepository.findCategory(FAKE_CATEGORY.id)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(FAKE_CATEGORY.id)
    }

    @Test
    fun checkFavoriteCategoriesSuccessCase() = runTest {
        val result = categoryRepository.addCategory(FAKE_FAVORITE_CATEGORY)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()

        val newResult = categoryRepository.findCategory(FAKE_FAVORITE_CATEGORY.id)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(FAKE_FAVORITE_CATEGORY.id)
        Truth.assertThat(foundData.isFavorite).isTrue()
    }
}