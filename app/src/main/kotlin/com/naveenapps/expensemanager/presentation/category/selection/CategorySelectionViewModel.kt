package com.naveenapps.expensemanager.presentation.category.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.ui.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategorySelectionViewModel @Inject constructor(
    getCategoriesUseCase: GetAllCategoryUseCase,
) : ViewModel() {

    private val _errorMessage = MutableSharedFlow<UiText>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<List<Category>>(emptyList())
    val selectedCategories = _selectedCategory.asStateFlow()

    init {

        getCategoriesUseCase.invoke().map { categories ->
            _categories.value = categories
            _selectedCategory.value = categories
        }.launchIn(viewModelScope)
    }

    fun clearChanges() {
        _selectedCategory.value = emptyList()
    }

    fun selectThisAccount(category: Category, selected: Boolean) {
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
}