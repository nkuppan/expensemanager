package com.naveenapps.expensemanager.feature.category.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CategorySelectionViewModel(
    getCategoriesUseCase: GetAllCategoryUseCase,
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories = _selectedCategory.asStateFlow()

    init {
        getCategoriesUseCase.invoke().map { categories ->
            _categories.value = categories
            if (_selectedCategory.value.isEmpty()) {
                _selectedCategory.value = categories
            }
        }.launchIn(viewModelScope)
    }

    fun clearChanges() {
        _selectedCategory.value = emptyList()
    }

    fun selectThisCategory(category: Category, selected: Boolean) {
        viewModelScope.launch {
            val selectedCategories = _selectedCategory.value.toMutableList()

            val selectedCategory = selectedCategories.firstOrNull {
                category.id == it.id
            }

            if (selectedCategory != null) {
                if (selected.not()) {
                    selectedCategories.remove(selectedCategory)
                }
            } else {
                if (selected) {
                    selectedCategories.add(category)
                }
            }

            _selectedCategory.value = selectedCategories
        }
    }

    fun selectAllThisCategory(categories: List<Category>) {
        if (categories.isEmpty()) {
            return
        }

        viewModelScope.launch {
            clearChanges()

            val selectedCategories = _selectedCategory.value.toMutableList()

            repeat(categories.size) {
                val category = categories[it]

                val selectedCategory = selectedCategories.firstOrNull {
                    category.id == it.id
                }

                if (selectedCategory == null) {
                    selectedCategories.add(category)
                }
            }

            _selectedCategory.value = selectedCategories
        }
    }
}
