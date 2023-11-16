package com.naveenapps.expensemanager.core.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllCategoryUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.CategoryRepository) {

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