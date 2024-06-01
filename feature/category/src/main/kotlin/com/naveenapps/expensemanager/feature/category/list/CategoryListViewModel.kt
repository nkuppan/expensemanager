package com.naveenapps.expensemanager.feature.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    getAllCategoryUseCase: GetAllCategoryUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CategoryListState(
            categories = emptyList(),
            filteredCategories = emptyList(),
            selectedTab = CategoryTabItems.Expense,
            tabs = CategoryTabItems.entries
        )
    )
    val state = _state.asStateFlow()

    init {
        getAllCategoryUseCase.invoke().onEach { categories ->
            updateCategories(categories, _state.value.selectedTab)
        }.launchIn(viewModelScope)
    }

    private fun updateCategories(
        totalCategories: List<Category>,
        categoryType: CategoryTabItems
    ) {
        val filteredCategories = totalCategories.filter { it.type == categoryType.categoryType }
        _state.update {
            it.copy(
                categories = totalCategories,
                filteredCategories = filteredCategories,
                selectedTab = categoryType
            )
        }
    }

    private fun openCreateScreen(categoryId: String?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.CategoryCreate(categoryId),
        )
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun setCategoryType(categoryType: CategoryTabItems) {
        updateCategories(_state.value.categories, categoryType)
    }

    fun processAction(action: CategoryListAction) {
        when (action) {
            CategoryListAction.ClosePage -> closePage()
            CategoryListAction.Create -> openCreateScreen(null)
            is CategoryListAction.Edit -> openCreateScreen(action.item.id)
            is CategoryListAction.ChangeCategory -> setCategoryType(action.type)
        }
    }
}
