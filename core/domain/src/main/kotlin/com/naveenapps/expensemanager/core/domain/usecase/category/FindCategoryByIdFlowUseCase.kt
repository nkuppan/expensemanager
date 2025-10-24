package com.naveenapps.expensemanager.core.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class FindCategoryByIdFlowUseCase(private val repository: CategoryRepository) {

    operator fun invoke(categoryId: String): Flow<Category?> {
        return repository.findCategoryFlow(categoryId)
    }
}
