package com.naveenapps.expensemanager.feature.category.selection

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.feature.category.list.getRandomCategoryData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CategorySelectionViewModelTest : BaseCoroutineTest() {

    private val repository: CategoryRepository = mock()

    private val getCategoriesUseCase: GetAllCategoryUseCase = GetAllCategoryUseCase(repository)

    private lateinit var categorySelectionViewModel: CategorySelectionViewModel

    override fun onCreate() {
        super.onCreate()
        whenever(repository.getCategories()).thenReturn(flowOf(getRandomCategoryData(5)))
        categorySelectionViewModel = CategorySelectionViewModel(getCategoriesUseCase)
    }

    @Test
    fun whenCategoryAvailableItShouldReflectOnSelectedCategories() = runTest {

        categorySelectionViewModel.categories.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).hasSize(5)
        }

        categorySelectionViewModel.selectedCategories.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).hasSize(5)
        }
    }

    @Test
    fun whenClearSelectedCategoryItShouldReflectOnSelectedCategoriesSize() = runTest {

        categorySelectionViewModel.selectedCategories.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).hasSize(5)

            categorySelectionViewModel.clearChanges()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isEmpty()
        }
    }

    @Test
    fun whenChangingSelectionOfTheCategoryItShouldReflectOnSelectedCategoriesSize() = runTest {

        categorySelectionViewModel.selectedCategories.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).hasSize(5)

            categorySelectionViewModel.selectThisCategory(firstItem.first(), false)

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem).isNotEmpty()
            Truth.assertThat(secondItem).hasSize(4)

            categorySelectionViewModel.selectThisCategory(firstItem.first(), true)

            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNotNull()
            Truth.assertThat(thirdItem).isNotEmpty()
            Truth.assertThat(thirdItem).hasSize(5)

        }
    }

    @Test
    fun whenSelectingListOfCategoryShouldReplacedExisting() = runTest {

        categorySelectionViewModel.selectedCategories.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).hasSize(5)

            categorySelectionViewModel.selectAllThisCategory(getRandomCategoryData(3))

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isEmpty()

            val thirdItem = awaitItem()
            Truth.assertThat(thirdItem).isNotEmpty()
            Truth.assertThat(thirdItem).hasSize(3)
        }
    }

    @Test
    fun whenSelectingEmptyListOfCategoryShouldNotReplacedExisting() = runTest {

        categorySelectionViewModel.selectedCategories.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem).isNotEmpty()
            Truth.assertThat(firstItem).hasSize(5)

            categorySelectionViewModel.selectAllThisCategory(emptyList())
        }
    }
}
