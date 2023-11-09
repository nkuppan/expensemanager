package com.naveenapps.expensemanager.domain.usecase.category

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.CategoryRepository
import javax.inject.Inject

class GetCategoryByNameUseCase @Inject constructor(
    private val repository: CategoryRepository
) {

    suspend operator fun invoke(categoryName: String?): Resource<List<Category>> {
        return when (val response = repository.getAllCategory()) {
            is Resource.Error -> response
            is Resource.Success -> {

                val values = response.data

                val filteredList = mutableListOf<Category>()

                if (categoryName?.isNotBlank() == true && values.isNotEmpty()) {
                    filteredList.addAll(values.filter {
                        it.name.contains(categoryName, ignoreCase = true)
                    })
                } else {
                    filteredList.addAll(values)
                }

                Resource.Success(filteredList)
            }
        }
    }
}