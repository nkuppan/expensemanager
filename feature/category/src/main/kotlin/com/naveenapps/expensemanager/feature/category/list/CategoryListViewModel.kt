package com.naveenapps.expensemanager.feature.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.UiState
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _categories = MutableStateFlow<UiState<List<Category>>>(UiState.Loading)
    val categories = _categories.asStateFlow()

    init {
        getAllCategoryUseCase.invoke().map {
            if (it.isEmpty()) {
                _categories.value = UiState.Empty
            } else {
                _categories.value = UiState.Success(it)
            }
        }.launchIn(viewModelScope)
    }

    fun openCreateScreen(categoryId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryCreate.createRoute(categoryId ?: "")
        )
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}