package com.naveenapps.expensemanager.core.data.repository

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
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import com.naveenapps.expensemanager.core.testing.FAKE_FAVORITE_CATEGORY
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CategoryRepositoryImplTest : BaseCoroutineTest() {

    private lateinit var categoryDao: CategoryDao

    private lateinit var database: ExpenseManagerDatabase

    private lateinit var categoryRepository: CategoryRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseManagerDatabase::class.java,
        ).allowMainThreadQueries().build()

        categoryDao = database.categoryDao()

        categoryRepository = CategoryRepositoryImpl(
            categoryDao,
            AppCoroutineDispatchers(
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
                testCoroutineDispatcher.dispatcher,
            ),
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
    fun updateCategorySuccessCase() = runTest {
        insertCategoryAndAssert(FAKE_FAVORITE_CATEGORY)
        val category = FAKE_FAVORITE_CATEGORY.copy(name = "Sample Notes")
        updateCategoryAndAssert(category)
        findCategoryAndAssert(category.id)
    }

    @Test
    fun getAllCategoryCase() = runTest {
        insertCategoryAndAssert(FAKE_FAVORITE_CATEGORY)
        val result = categoryRepository.getAllCategory()
        Truth.assertThat(result).isNotNull()
        Truth.assertThat(result).isInstanceOf(Resource.Success::class.java)
        Truth.assertThat((result as Resource.Success).data).hasSize(1)
    }

    @Test
    fun checkCategoryChangeFlow() = runTest {
        insertCategoryAndAssert(FAKE_FAVORITE_CATEGORY)

        categoryRepository.findCategoryFlow(FAKE_FAVORITE_CATEGORY.id).test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem?.id).isEqualTo(FAKE_FAVORITE_CATEGORY.id)

            val updated = FAKE_FAVORITE_CATEGORY.copy(name = "Updated")
            categoryRepository.updateCategory(updated)

            val updatedItem = awaitItem()
            Truth.assertThat(updatedItem).isNotNull()
            Truth.assertThat(updatedItem?.id).isEqualTo(updated.id)
            Truth.assertThat(updatedItem?.name).isEqualTo(updated.name)
        }
    }

    @Test
    fun checkGetAllCategoryFlowAfterInsertCase() = runTest {
        categoryRepository.getCategories().test {
            val data = awaitItem()
            Truth.assertThat(data).isEmpty()

            insertCategoryAndAssert(FAKE_CATEGORY)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotEmpty()
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

    private suspend fun updateCategoryAndAssert(category: Category) {
        val result = categoryRepository.updateCategory(category)
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
