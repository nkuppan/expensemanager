package com.naveenapps.expensemanager.feature.category.selection

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
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

    @Test
    fun incomeCategoriesAreListedBeforeExpenseCategories() = runTest {
        val rent = FAKE_CATEGORY.copy(id = "rent", name = "Rent", type = CategoryType.EXPENSE)
        val salary = FAKE_CATEGORY.copy(id = "salary", name = "Salary", type = CategoryType.INCOME)
        val food = FAKE_CATEGORY.copy(id = "food", name = "Food", type = CategoryType.EXPENSE)
        val bonus = FAKE_CATEGORY.copy(id = "bonus", name = "Bonus", type = CategoryType.INCOME)
        whenever(repository.getCategories()).thenReturn(flowOf(listOf(rent, salary, food, bonus)))

        val viewModel = CategorySelectionViewModel(getCategoriesUseCase)

        viewModel.categories.test {
            // Income first, then expense; each group stays alphabetical.
            Truth.assertThat(awaitItem().map { it.id }).containsExactly("bonus", "salary", "food", "rent").inOrder()
        }
    }

    @Test
    fun selectAllForListedItemsLeavesOtherSelectionsAlone() = runTest {
        categorySelectionViewModel.selectedCategories.test {
            val all = awaitItem()
            Truth.assertThat(all).hasSize(5)

            // Untick the first two (e.g. what a search shows), then tick them again.
            categorySelectionViewModel.setSelection(all.take(2), false)
            Truth.assertThat(awaitItem().map { it.id }).containsExactlyElementsIn(all.drop(2).map { it.id })

            categorySelectionViewModel.setSelection(all.take(2), true)
            Truth.assertThat(awaitItem().map { it.id }).containsExactlyElementsIn(all.map { it.id })
        }
    }
}
