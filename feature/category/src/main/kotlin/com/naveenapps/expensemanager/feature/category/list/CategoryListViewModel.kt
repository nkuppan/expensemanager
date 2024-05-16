package com.naveenapps.expensemanager.feature.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories = _categories.asStateFlow()

    private val categoryType = MutableStateFlow(CategoryType.EXPENSE)

    init {
        combine(
            categoryType,
            getAllCategoryUseCase.invoke(),
        ) { categoryType, categories ->
            val categoryList = categories.filter { it.type == categoryType }
            if (categoryList.isEmpty()) {
                _categories.value = UiState.Empty
            } else {
                _categories.value = UiState.Success(categoryList)
            }
        }.launchIn(viewModelScope)
    }

    fun openCreateScreen(categoryId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryCreate(categoryId),
        )
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun setCategoryType(categoryType: CategoryType) {
        this.categoryType.value = categoryType
    }
}
