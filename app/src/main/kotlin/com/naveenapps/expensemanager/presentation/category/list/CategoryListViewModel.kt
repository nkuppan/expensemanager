package com.naveenapps.expensemanager.presentation.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.domain.model.Category
import com.naveenapps.expensemanager.domain.model.UiState
import com.naveenapps.expensemanager.domain.usecase.category.GetAllCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    getAllCategoryUseCase: GetAllCategoryUseCase
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
}