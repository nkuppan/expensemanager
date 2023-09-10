package com.nkuppan.expensemanager.presentation.category.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.usecase.category.DeleteCategoryUseCase
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