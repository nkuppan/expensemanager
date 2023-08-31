package com.nkuppan.expensemanager.data.usecase.category

import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {

    operator fun invoke(): Flow<List<Category>> {
        return repository.getCategories()
    }
}