package com.nkuppan.expensemanager.feature.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.core.model.UiState
import com.nkuppan.expensemanager.data.usecase.category.GetAllCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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