package com.naveenapps.expensemanager.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import javax.inject.Inject

class FindCategoryByIdUseCase @Inject constructor(
    private val repository: com.naveenapps.expensemanager.core.data.repository.CategoryRepository
) {

    suspend operator fun invoke(categoryId: String?): Resource<Category> {

        if (categoryId.isNullOrBlank()) {
            return Resource.Error(Exception("Provide valid category id value"))
        }

        return repository.findCategory(categoryId)
    }
}