package com.naveenapps.expensemanager.core.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
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

    private lateinit var categoryRepository: com.naveenapps.expensemanager.core.domain.repository.CategoryRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), ExpenseManagerDatabase::class.java
        ).allowMainThreadQueries().build()

        categoryDao = database.categoryDao()

        categoryRepository = CategoryRepositoryImpl(
            categoryDao, AppCoroutineDispatchers(
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
        insertCategoryAndAssert(FAKE_CATEGORY)
    }

    @Test
    fun checkDeleteSuccessCase() = runTest {
        insertCategoryAndAssert(FAKE_CATEGORY)
        deleteCategoryAndAssert(FAKE_CATEGORY)
    }

    @Test
    fun checkFindByIdSuccessCase() = runTest {
        insertCategoryAndAssert(FAKE_CATEGORY)
        findCategoryAndAssert(FAKE_CATEGORY.id)
    }

    @Test
    fun checkGetAllCategoriesSuccessCase() = runTest {
        insertCategoryAndAssert(FAKE_CATEGORY)
        findCategoryAndAssert(FAKE_CATEGORY.id)
    }

    @Test
    fun checkFavoriteCategoriesSuccessCase() = runTest {
        insertCategoryAndAssert(FAKE_FAVORITE_CATEGORY)
        findCategoryAndAssert(FAKE_FAVORITE_CATEGORY.id)
    }

    @Test
    fun checkFindCategoryErrorCase() = runTest {
        val newResult = categoryRepository.findCategory("Unknown id")
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Error::class.java)
        val foundData = (newResult as Resource.Error).exception
        Truth.assertThat(foundData).isNotNull()
    }

    @Test
    fun checkGetAllCategoryFlowAfterInsertCase() = runTest {
        categoryRepository.getCategories().test {
            val data = awaitItem()
            Truth.assertThat(data).isEmpty()

            insertCategoryAndAssert(FAKE_CATEGORY)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isEmpty()
            val firstItem = secondItem.first()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.id).isEqualTo(FAKE_CATEGORY.id)

            deleteCategoryAndAssert(FAKE_CATEGORY)
            val newData = awaitItem()
            Truth.assertThat(newData).isEmpty()
        }
    }

    private suspend fun findCategoryAndAssert(categoryId: String) {
        val newResult = categoryRepository.findCategory(categoryId)
        Truth.assertThat(newResult).isNotNull()
        Truth.assertThat(newResult).isInstanceOf(Resource.Success::class.java)
        val foundData = (newResult as Resource.Success).data
        Truth.assertThat(foundData).isNotNull()
        Truth.assertThat(foundData.id).isEqualTo(categoryId)
    }

    private suspend fun insertCategoryAndAssert(category: Category) {
        val result = categoryRepository.addCategory(category)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }

    private suspend fun deleteCategoryAndAssert(category: Category) {
        val result = categoryRepository.deleteCategory(category)
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        val data = (result as Resource.Success).data
        Truth.assertThat(data).isNotNull()
        Truth.assertThat(data).isTrue()
    }
}