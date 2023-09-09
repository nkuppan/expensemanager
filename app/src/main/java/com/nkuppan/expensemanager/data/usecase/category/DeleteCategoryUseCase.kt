package com.nkuppan.expensemanager.data.usecase.category

import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val checkCategoryValidationUseCase: CheckCategoryValidationUseCase
) {

    suspend operator fun invoke(category: Category): Resource<Boolean> {
        return when (val validationResult = checkCategoryValidationUseCase(category)) {
            is Resource.Error -> {
                validationResult
            }

            is Resource.Success -> {
                repository.deleteCategory(category)
            }
        }
    }
}