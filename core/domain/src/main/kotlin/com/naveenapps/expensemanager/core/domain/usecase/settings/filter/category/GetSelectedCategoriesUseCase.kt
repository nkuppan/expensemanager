package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.category

import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdUseCase
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSelectedCategoriesUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val findCategoryByIdUseCase: FindCategoryByIdUseCase
) {

    operator fun invoke(): Flow<List<Category>> {
        return settingsRepository.getCategories().map { categoryIds ->
            return@map buildList<Category> {
                if (categoryIds?.isNotEmpty() == true) {
                    repeat(categoryIds.size) {
                        val categoryId = categoryIds[it]
                        when (val response = findCategoryByIdUseCase.invoke(categoryId)) {
                            is Resource.Error -> Unit
                            is Resource.Success -> {
                                add(response.data)
                            }
                        }
                    }
                }
            }
        }
    }
}