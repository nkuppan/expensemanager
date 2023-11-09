package com.naveenapps.expensemanager.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
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