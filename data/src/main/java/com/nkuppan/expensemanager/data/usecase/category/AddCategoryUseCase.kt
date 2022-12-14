package com.nkuppan.expensemanager.data.usecase.category

import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.CategoryRepository

class AddCategoryUseCase(
    private val repository: CategoryRepository,
    private val checkCategoryValidationUseCase: CheckCategoryValidationUseCase
) {

    suspend operator fun invoke(category: Category): Resource<Boolean> {
        return when (val validationResult = checkCategoryValidationUseCase(category)) {
            is Resource.Error -> {
                validationResult
            }
            is Resource.Success -> {
                repository.addCategory(category)
            }
        }
    }
}