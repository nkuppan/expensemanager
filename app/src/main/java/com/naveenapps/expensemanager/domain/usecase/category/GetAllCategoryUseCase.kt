package com.naveenapps.expensemanager.domain.usecase.category

import com.naveenapps.expensemanager.domain.model.Category
import com.naveenapps.expensemanager.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {

    operator fun invoke(): Flow<List<Category>> {
        return repository.getCategories()
    }
}