package com.nkuppan.expensemanager.domain.usecase.category

import com.nkuppan.expensemanager.domain.model.Category
import com.nkuppan.expensemanager.domain.model.Resource
import javax.inject.Inject

class CheckCategoryValidationUseCase @Inject constructor() {

    operator fun invoke(category: Category): Resource<Boolean> {

        if (category.id.isBlank()) {
            return Resource.Error(Exception("Please specify the category id"))
        }

        if (category.iconBackgroundColor.isBlank()) {
            return Resource.Error(Exception("Background color is not available"))
        }

        if (!category.iconBackgroundColor.startsWith("#")) {
            return Resource.Error(Exception("Background color is not valid"))
        }

        return Resource.Success(true)
    }
}