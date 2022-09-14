package com.nkuppan.expensemanager.feature.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.usecase.category.GetCategoryByNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    private val getCategoryByNameUseCase: GetCategoryByNameUseCase
) : ViewModel() {

    private val _categories = Channel<List<Category>>()
    val categories: Flow<List<Category>> = _categories.receiveAsFlow()

    fun loadCategories(searchValue: String? = "") {

        viewModelScope.launch {

            when (val result = getCategoryByNameUseCase.invoke(searchValue)) {
                is Resource.Error -> {
                    _categories.send(emptyList())
                }
                is Resource.Success -> {
                    _categories.send(result.data)
                }
            }
        }
    }
}