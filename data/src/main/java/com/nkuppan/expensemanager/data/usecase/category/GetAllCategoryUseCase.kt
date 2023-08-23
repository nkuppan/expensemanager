package com.nkuppan.expensemanager.data.usecase.category

import com.nkuppan.expensemanager.core.model.Category
import com.nkuppan.expensemanager.core.model.Resource
import com.nkuppan.expensemanager.data.repository.CategoryRepository
import javax.inject.Inject

class GetAllCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository
) {

    suspend operator fun invoke(): Resource<List<Category>> {
        return repository.getAllCategory()
    }
}