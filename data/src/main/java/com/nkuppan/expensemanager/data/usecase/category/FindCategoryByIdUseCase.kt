package com.nkuppan.expensemanager.data.usecase.category

import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.CategoryRepository
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