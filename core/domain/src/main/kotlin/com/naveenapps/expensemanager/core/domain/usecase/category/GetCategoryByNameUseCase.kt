package com.naveenapps.expensemanager.core.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CategoryRepository

class GetCategoryByNameUseCase(private val repository: CategoryRepository) {

    suspend operator fun invoke(categoryName: String?): Resource<List<Category>> {
        return when (val response = repository.getAllCategory()) {
            is Resource.Error -> response
            is Resource.Success -> {
                val values = response.data

                val filteredList = mutableListOf<Category>()

                if (categoryName?.isNotBlank() == true && values.isNotEmpty()) {
                    filteredList.addAll(
                        values.filter {
                            it.name.contains(categoryName, ignoreCase = true)
                        },
                    )
                } else {
                    filteredList.addAll(values)
                }

                Resource.Success(filteredList)
            }
        }
    }
}
