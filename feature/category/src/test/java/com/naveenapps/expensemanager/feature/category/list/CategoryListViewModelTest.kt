package com.naveenapps.expensemanager.feature.category.list

import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.repository.CategoryRepository
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CategoryListViewModelTest : BaseCoroutineTest() {

    private val categoryRepository: CategoryRepository = mock()

    private val getAllCategoryUseCase: GetAllCategoryUseCase = GetAllCategoryUseCase(
        categoryRepository
    )

    private val appComposeNavigator: AppComposeNavigator = mock()

    private lateinit var categoryListViewModel: CategoryListViewModel

    private val categoryFlow = MutableStateFlow<List<Category>>(emptyList())

    override fun onCreate() {

        whenever(categoryRepository.getCategories()).thenReturn(categoryFlow)

        categoryListViewModel = CategoryListViewModel(
            getAllCategoryUseCase,
            appComposeNavigator
        )
    }


    @Test
    fun categorySuccess() = runTest {

        val totalCount = 20

        categoryFlow.value = getRandomCategoryData(totalCount)

        categoryListViewModel.categories.test {
            val loadingState = awaitItem()
            Truth.assertThat(loadingState).isNotNull()
            Truth.assertThat(loadingState).isInstanceOf(UiState.Loading::class.java)

            val state = awaitItem()
            Truth.assertThat(state).isNotNull()
            Truth.assertThat(state).isInstanceOf(UiState.Success::class.java)
            val categories = (state as UiState.Success).data
            Truth.assertThat(categories).isNotEmpty()
        }
    }


    @Test
    fun categoryEmpty() = runTest {

        categoryFlow.value = emptyList()

        categoryListViewModel.categories.test {
            val loadingState = awaitItem()
            Truth.assertThat(loadingState).isNotNull()
            Truth.assertThat(loadingState).isInstanceOf(UiState.Loading::class.java)

            val state = awaitItem()
            Truth.assertThat(state).isNotNull()
            Truth.assertThat(state).isInstanceOf(UiState.Empty::class.java)
        }
    }

    @Test
    fun categorySuccessAndTypeSwitch() = runTest {

        val totalCount = 20

        val randomCategoryData = getRandomCategoryData(totalCount)
        val expectedExpenseCount = randomCategoryData.count { it.type.isExpense() }
        val expectedIncomeCount = randomCategoryData.count { it.type.isIncome() }
        categoryFlow.value = randomCategoryData

        categoryListViewModel.categories.test {
            val loadingState = awaitItem()
            Truth.assertThat(loadingState).isNotNull()
            Truth.assertThat(loadingState).isInstanceOf(UiState.Loading::class.java)

            val state = awaitItem()
            Truth.assertThat(state).isNotNull()
            Truth.assertThat(state).isInstanceOf(UiState.Success::class.java)
            val categories = (state as UiState.Success).data
            Truth.assertThat(categories).isNotEmpty()

            val actualExpenseCount = categories.count { it.type.isExpense() }
            Truth.assertThat(actualExpenseCount).isEqualTo(expectedExpenseCount)

            categoryListViewModel.setCategoryType(CategoryType.INCOME)

            val incomeState = awaitItem()
            Truth.assertThat(incomeState).isNotNull()
            Truth.assertThat(incomeState).isInstanceOf(UiState.Success::class.java)
            val incomeCategories = (incomeState as UiState.Success).data
            Truth.assertThat(incomeCategories).isNotEmpty()

            val actualIncomeCount = incomeCategories.count { it.type.isIncome() }
            Truth.assertThat(actualIncomeCount).isEqualTo(expectedIncomeCount)
        }
    }

    @Test
    fun checkOpenCreateNavigation() = runTest {
        categoryListViewModel.openCreateScreen("")
        verify(appComposeNavigator, times(1)).navigate(anyString(), eq(null))
    }

    @Test
    fun checkOpenCreateNavigationAndCommands() = runTest {
        categoryListViewModel.openCreateScreen("")
        verify(appComposeNavigator, times(1)).navigate(anyString(), eq(null))
        appComposeNavigator
    }

    @Test
    fun checkClosePageNavigation() = runTest {
        categoryListViewModel.closePage()
        verify(appComposeNavigator, times(1)).popBackStack()
    }
}