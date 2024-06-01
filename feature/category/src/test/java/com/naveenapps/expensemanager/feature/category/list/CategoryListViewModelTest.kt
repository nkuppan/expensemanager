package com.naveenapps.expensemanager.feature.category.list

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CategoryListViewModelTest : BaseCoroutineTest() {

    private val categoryRepository: CategoryRepository = mock()

    private val getAllCategoryUseCase: GetAllCategoryUseCase = GetAllCategoryUseCase(
        categoryRepository,
    )

    private val appComposeNavigator: AppComposeNavigator = mock()

    private lateinit var categoryListViewModel: CategoryListViewModel

    private val categoryFlow = MutableStateFlow<List<Category>>(emptyList())

    override fun onCreate() {
        whenever(categoryRepository.getCategories()).thenReturn(categoryFlow)

        categoryListViewModel = CategoryListViewModel(
            getAllCategoryUseCase,
            appComposeNavigator,
        )
    }

    @Test
    fun categorySuccess() = runTest {
        val totalCount = 20

        categoryFlow.value = getRandomCategoryData(totalCount)

        categoryListViewModel.state.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem.categories).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem.categories).isNotEmpty()
            Truth.assertThat(secondItem.categories).hasSize(totalCount)
            Truth.assertThat(secondItem.filteredCategories).hasSize(totalCount / 2)
        }
    }

    @Test
    fun categoryEmpty() = runTest {
        categoryFlow.value = emptyList()

        categoryListViewModel.state.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem.categories).isNotNull()
            Truth.assertThat(firstItem.categories).isEmpty()
        }
    }

    @Test
    fun checkOpenCreateNavigation() = runTest {
        categoryListViewModel.processAction(CategoryListAction.Create)
        verify(appComposeNavigator, times(1)).navigate(any())
    }

    @Test
    fun checkOpenCreateNavigationAndCommands() = runTest {
        categoryListViewModel.processAction(CategoryListAction.Edit(FAKE_CATEGORY))
        verify(appComposeNavigator, times(1)).navigate(any())
    }

    @Test
    fun checkClosePageNavigation() = runTest {
        categoryListViewModel.processAction(CategoryListAction.ClosePage)
        verify(appComposeNavigator, times(1)).popBackStack()
    }
}
