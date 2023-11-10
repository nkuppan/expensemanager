package com.naveenapps.expensemanager.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCategoryUseCase @Inject constructor(private val repository: com.naveenapps.expensemanager.core.data.repository.CategoryRepository) {

    operator fun invoke(): Flow<List<Category>> {
        return repository.getCategories()
    }
}