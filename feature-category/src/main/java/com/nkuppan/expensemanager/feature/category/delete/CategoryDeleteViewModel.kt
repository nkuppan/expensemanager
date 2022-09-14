package com.nkuppan.expensemanager.feature.category.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.usecase.category.DeleteCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryDeleteViewModel @Inject constructor(
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _deleted = Channel<Boolean>()
    val deleted = _deleted.receiveAsFlow()

    fun deleteCategory(category: Category) {

        viewModelScope.launch {

            when (val result = deleteCategoryUseCase.invoke(category = category)) {
                is Resource.Error -> {
                    _deleted.send(false)
                }
                is Resource.Success -> {
                    _deleted.send(result.data)
                }
            }
        }
    }
}