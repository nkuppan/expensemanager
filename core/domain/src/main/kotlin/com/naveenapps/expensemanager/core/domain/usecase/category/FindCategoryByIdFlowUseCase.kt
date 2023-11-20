package com.naveenapps.expensemanager.core.domain.usecase.category

import com.naveenapps.expensemanager.core.data.repository.CategoryRepository
import com.naveenapps.expensemanager.core.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCategoryByIdFlowUseCase @Inject constructor(private val repository: CategoryRepository) {

    operator fun invoke(categoryId: String): Flow<Category?> {
        return repository.findCategoryFlow(categoryId)
    }
}