package com.naveenapps.expensemanager.core.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAllCategoryUseCase(private val repository: CategoryRepository) {

    operator fun invoke(): Flow<List<Category>> {
        return repository.getCategories().map { categories ->
            if (categories.isNotEmpty()) {
                categories.sortedBy { it.name }
            } else {
                categories
            }
        }
    }
}
