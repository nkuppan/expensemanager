package com.naveenapps.expensemanager.core.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CategoryRepository

class FindCategoryByIdUseCase(private val repository: CategoryRepository) {

    suspend operator fun invoke(categoryId: String?): Resource<Category> {
        if (categoryId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid category id value"))
        }

        return repository.findCategory(categoryId)
    }
}
