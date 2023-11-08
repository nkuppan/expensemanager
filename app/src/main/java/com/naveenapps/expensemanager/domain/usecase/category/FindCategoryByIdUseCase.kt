package com.naveenapps.expensemanager.domain.usecase.category

import com.naveenapps.expensemanager.domain.model.Category
import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.repository.CategoryRepository
import javax.inject.Inject

class FindCategoryByIdUseCase @Inject constructor(
    private val repository: CategoryRepository
) {

    suspend operator fun invoke(categoryId: String?): Resource<Category> {

        if (categoryId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid category id value"))
        }

        return repository.findCategory(categoryId)
    }
}