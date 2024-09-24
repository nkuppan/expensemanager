package com.naveenapps.expensemanager.feature.category.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategorySelectionViewModel @Inject constructor(
    getCategoriesUseCase: GetAllCategoryUseCase,
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories = _selectedCategories.asStateFlow()

    init {
        getCategoriesUseCase.invoke()
                .map { categories ->
                    _categories.value = categories
                    if (_selectedCategories.value.isEmpty()) {
                        _selectedCategories.value = categories
                    }
                }
                .launchIn(viewModelScope)
    }

    fun clearChanges() {
        _selectedCategories.value = emptyList()
    }

    fun selectThisCategory(category: Category, selected: Boolean) {
        viewModelScope.launch {
            val selectedCategories = _selectedCategories.value.toMutableList()

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

            _selectedCategories.value = selectedCategories
        }
    }

    fun selectAllTheseCategories(categories: List<Category>) {
        categories.ifEmpty { return }

        viewModelScope.launch {
            clearChanges()
            _selectedCategories.value = categories
        }

    }
}
