package com.naveenapps.expensemanager.feature.category.create

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.category.AddCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.CheckCategoryValidationUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.DeleteCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.UpdateCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_CATEGORY
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever


class CategoryCreateViewModelTest : BaseCoroutineTest() {

    @Captor
    var categoryCapture: ArgumentCaptor<Category> =
        ArgumentCaptor.forClass(Category::class.java)

    private var categoryRepository: CategoryRepository = mock()
    private var appComposeNavigator: AppComposeNavigator = mock()

    private lateinit var categoryCreateViewModel: CategoryCreateViewModel

    private fun createCategoryViewModel(state: Map<String, Any?>) {

        val checkCategoryValidationUseCase = CheckCategoryValidationUseCase()

        categoryCreateViewModel = CategoryCreateViewModel(
            savedStateHandle = SavedStateHandle(state),
            findCategoryByIdUseCase = FindCategoryByIdUseCase(categoryRepository),
            addCategoryUseCase = AddCategoryUseCase(
                categoryRepository,
                checkCategoryValidationUseCase
            ),
            updateCategoryUseCase = UpdateCategoryUseCase(
                categoryRepository,
                checkCategoryValidationUseCase
            ),
            deleteCategoryUseCase = DeleteCategoryUseCase(
                categoryRepository,
                checkCategoryValidationUseCase
            ),
            appComposeNavigator = appComposeNavigator
        )
    }

    @Test
    fun `when user doesn't have a category it shouldn't update state`() = runTest {

        createCategoryViewModel(emptyMap())

        categoryCreateViewModel.state.test {
            val firstItem = awaitItem()

            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.name.value).isEmpty()
            Truth.assertThat(firstItem.type.value).isEqualTo(CategoryType.EXPENSE)
            Truth.assertThat(firstItem.color.value).isEqualTo("#43A546")
            Truth.assertThat(firstItem.icon.value).isEqualTo("ic_calendar")
        }
    }

    @Test
    fun `when user have a category it should update state`() = runTest {

        whenever(categoryRepository.findCategory(FAKE_CATEGORY.id)).thenReturn(
            Resource.Success(FAKE_CATEGORY)
        )

        createCategoryViewModel(mapOf(ExpenseManagerArgsNames.ID to FAKE_CATEGORY.id))

        categoryCreateViewModel.state.test {
            val firstItem = awaitItem()
            Truth.assertThat(firstItem).isNotNull()
            Truth.assertThat(firstItem.name.value).isEmpty()

            val secondItem = awaitItem()
            Truth.assertThat(secondItem).isNotNull()
            Truth.assertThat(secondItem.name.value).isEqualTo(FAKE_CATEGORY.name)
            Truth.assertThat(secondItem.type.value).isEqualTo(FAKE_CATEGORY.type)
            Truth.assertThat(secondItem.icon.value).isEqualTo(FAKE_CATEGORY.storedIcon.name)
            Truth.assertThat(secondItem.color.value)
                .isEqualTo(FAKE_CATEGORY.storedIcon.backgroundColor)
        }
    }

    @Test
    fun `when user try to save without proper value it shouldn't allow to save`() = runTest {

        createCategoryViewModel(emptyMap())

        categoryCreateViewModel.processAction(CategoryCreateAction.Save)

        verifyNoInteractions(categoryRepository)
        verifyNoInteractions(appComposeNavigator)
    }

    @Test
    fun `when user try to save with proper value it shouldn't allow to save`() = runTest {

        whenever(categoryRepository.addCategory(any())).thenReturn(Resource.Success(true))

        createCategoryViewModel(emptyMap())

        categoryCreateViewModel.state.value.name.onValueChange?.invoke("Category")
        categoryCreateViewModel.state.value.type.onValueChange?.invoke(CategoryType.EXPENSE)

        advanceUntilIdle()

        categoryCreateViewModel.processAction(CategoryCreateAction.Save)

        advanceUntilIdle()

        verify(categoryRepository, times(1)).addCategory(capture(categoryCapture))
        verify(appComposeNavigator, times(1)).popBackStack()

        Truth.assertThat(categoryCapture.value.name).isEqualTo("Category")
        Truth.assertThat(categoryCapture.value.type).isEqualTo(CategoryType.EXPENSE)
        Truth.assertThat(categoryCapture.value.storedIcon.backgroundColor).isEqualTo("#43A546")
        Truth.assertThat(categoryCapture.value.storedIcon.name).isEqualTo("ic_calendar")
    }

    @Test
    fun `when user try to edit and save with proper value it shouldn't allow to save`() = runTest {

        whenever(categoryRepository.updateCategory(any())).thenReturn(Resource.Success(true))

        whenever(categoryRepository.findCategory(FAKE_CATEGORY.id)).thenReturn(
            Resource.Success(FAKE_CATEGORY)
        )

        createCategoryViewModel(mapOf(ExpenseManagerArgsNames.ID to FAKE_CATEGORY.id))

        advanceUntilIdle()

        categoryCreateViewModel.state.value.name.onValueChange?.invoke("Category")
        categoryCreateViewModel.state.value.type.onValueChange?.invoke(CategoryType.EXPENSE)

        advanceUntilIdle()

        categoryCreateViewModel.processAction(CategoryCreateAction.Save)

        advanceUntilIdle()

        verify(categoryRepository, times(1)).updateCategory(capture(categoryCapture))
        verify(appComposeNavigator, times(1)).popBackStack()

        Truth.assertThat(categoryCapture.value.name).isEqualTo("Category")
        Truth.assertThat(categoryCapture.value.type).isEqualTo(CategoryType.EXPENSE)
        Truth.assertThat(categoryCapture.value.storedIcon.backgroundColor)
            .isEqualTo(FAKE_CATEGORY.storedIcon.backgroundColor)
        Truth.assertThat(categoryCapture.value.storedIcon.name)
            .isEqualTo(FAKE_CATEGORY.storedIcon.name)
    }
}